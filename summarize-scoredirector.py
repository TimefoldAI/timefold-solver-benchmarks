#!/usr/bin/env python3
"""Aggregate the score-director benchmark subjobs into one regression table.

Each CI subjob (one per example) uploads a "data-<example>" artifact holding
"baseline-results.json" and "sut-results.json" - the raw JMH JSON output for that one example. This
script joins every such pair on example, computes the speed difference, marks each row that
regresses or improves, and prints one markdown table plus a legend. Its exit code is the CI verdict:
0 only if every expected row is present and either within tolerance or an improvement.

Each row also reports its resolution: how small a difference this particular run could have told
apart from zero. That is the trust signal - a delta only means something when the run that measured
it could resolve something that size.

Usage:
  python3 summarize-scoredirector.py DATA_DIR --expect JSON_ARRAY --baseline REF --branch REF
                                     --owner OWNER
  python3 summarize-scoredirector.py --selftest
"""
import argparse
import collections
import glob
import importlib.util
import json
import math
import os
import re
import statistics
import sys

# The band inside which a delta counts as noise. Measured, not guessed: across 36 main-against-main
# comparisons the deltas had an RMS of 0.55 % and a worst case of 1.61 %, so 3 % leaves about a
# factor of two over anything actually seen and lets through roughly one false failure in a
# thousand runs. Narrower is not free: the noise is per-fork and independent, so the only way to
# buy a tighter band is to spend forks. Re-measure this whenever the runners change.
TOLERANCE_PCT = 3.0

RUNNER_LABEL = "ubuntu-24.04-arm"

UNRESOLVED = "⚠️"
ABSENT = "—"

def _load_merge_module():
    """The t quantile lives in the merge script, which needs the same 99.9 % convention JMH uses.

    Both scripts sit side by side in the repository root and are checked out together, but the
    file name has hyphens, so it cannot be imported by name.
    """
    path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "merge-scoredirector-results.py")
    spec = importlib.util.spec_from_file_location("merge_scoredirector_results", path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


_MERGE = _load_merge_module()

Verdict = collections.namedtuple("Verdict", ("emoji", "label"))

REGRESSION = Verdict("‼️", "regression")
UNDETERMINED = Verdict("⁉️", "undetermined")
IMPROVEMENT = Verdict("🚀", "improvement")
TOLERANCE = Verdict("✅", "within tolerance")
MISSING = Verdict("❌", "missing data")

_ALL_VERDICTS = (REGRESSION, UNDETERMINED, MISSING, IMPROVEMENT, TOLERANCE)
_FAILING_VERDICTS = {REGRESSION, UNDETERMINED, MISSING}


def _to_float(value) -> float:
    """JMH writes NaN both as the bare literal and as the string "NaN"; either parses cleanly."""
    return float(value)


def load_side(path: str) -> dict:
    """Reads one results.json, keyed by example name (e.g. "cloud_balancing").

    The example is the value of whichever "*Example" param the JMH entry carries
    (ConstraintStreamsBenchmark.csExample, or ConstraintStreamsJustifiedBenchmark.csJustifiedExample),
    lowercased to match the CI matrix key. AbstractConfiguration#parseExamples matches example names
    case-insensitively, so this round-trips.
    """
    with open(path) as f:
        entries = json.load(f)
    result = {}
    for entry in entries:
        params = entry["params"]
        example_key = next(key for key in params if key.endswith("Example"))
        example = params[example_key].lower()
        metric = entry["primaryMetric"]
        conf = metric["scoreConfidence"]
        result[example] = {
            "score": _to_float(metric["score"]),
            "score_error": _to_float(metric["scoreError"]),
            "conf_lo": _to_float(conf[0]),
            "conf_hi": _to_float(conf[1]),
            # One value for each fork, in the order they ran, which is what pairs the two sides.
            "forks": [_to_float(value) for fork in metric.get("rawData", []) for value in fork],
        }
    return result


def load_all(data_dir: str) -> tuple[dict, dict, dict]:
    """The third dict maps example -> assets artifact URL (one per example subjob, covering both
    baseline and SUT - see "Archive benchmark assets" in the workflow)."""
    baseline, sut, asset_urls = {}, {}, {}
    for baseline_path in glob.glob(os.path.join(data_dir, "*", "baseline-results.json")):
        example_dir = os.path.dirname(baseline_path)
        sut_path = os.path.join(example_dir, "sut-results.json")
        baseline.update(load_side(baseline_path))
        if os.path.exists(sut_path):
            sut.update(load_side(sut_path))
        url_path = os.path.join(example_dir, "assets-url.txt")
        if os.path.exists(url_path):
            url = open(url_path).read().strip()
            if url:
                for example in load_side(baseline_path):
                    asset_urls[example] = url
    return baseline, sut, asset_urls


def relative_error(side: dict) -> float:
    if side["score"] == 0:
        return math.nan
    return abs(side["score_error"] / side["score"])


def resolution(old: dict, new: dict) -> float:
    """Half-width of the 99.9 % confidence interval on the delta, in percent. NaN when unknowable.

    Paired, because the workflow alternates: fork k of one side runs about a minute before fork k
    of the other, so both met the same machine. Taking the ratio inside each pair cancels whatever
    the machine was doing between them. The two forks of a pair correlate at +0.39 here, so the
    pairing is worth about a third of the variance; most of the spread is a per-JVM constant that
    no schedule can cancel. Pairing is never worse than not pairing, so it is unconditional.

    A side carrying no rawData cannot be paired, and neither can two sides of different lengths,
    which is what a retried fork leaves behind; both give NaN.
    """
    old_forks, new_forks = old["forks"], new["forks"]
    if len(old_forks) != len(new_forks) or len(old_forks) < 2:
        return math.nan
    if any(value <= 0 for value in old_forks + new_forks):
        return math.nan
    ratios = [math.log(after / before) for before, after in zip(old_forks, new_forks)]
    quantile = _MERGE.t_quantile(_MERGE.CONFIDENCE_TAIL, len(ratios) - 1)
    return quantile * statistics.stdev(ratios) / math.sqrt(len(ratios)) * 100


def evaluate_row(old: "dict | None", new: "dict | None") -> tuple["float | None", Verdict, float]:
    """Returns (delta_pct, verdict, resolution_pct). delta_pct is None only for MISSING."""
    if old is None or new is None:
        return None, MISSING, math.nan
    delta_pct = (new["score"] / old["score"] - 1) * 100
    if abs(delta_pct) <= TOLERANCE_PCT:
        verdict = TOLERANCE
    elif new["conf_lo"] > old["conf_hi"]:
        verdict = IMPROVEMENT
    elif old["conf_lo"] > new["conf_hi"]:
        verdict = REGRESSION
    else:
        verdict = UNDETERMINED
    return delta_pct, verdict, resolution(old, new)


def is_unresolved(resolution_pct: float) -> bool:
    """True when the run could not have told a band-sized change apart from no change at all."""
    return not math.isnan(resolution_pct) and resolution_pct >= TOLERANCE_PCT


def format_count(value: float) -> str:
    """Thin-space thousands separators, so the numbers stay readable in a narrow cell."""
    return format(round(value), ",").replace(",", " ")


def format_throughput(old: "dict | None", new: "dict | None", delta_pct: "float | None",
                       verdict: Verdict, resolution_pct: float) -> str:
    marker = verdict.emoji + (" " + UNRESOLVED if is_unresolved(resolution_pct) else "")
    if old is None or new is None:
        return f"{marker} {ABSENT} → {ABSENT}"
    return f"{marker} {format_count(old['score'])} → {format_count(new['score'])} ({delta_pct:+.1f} %)"


def format_resolution(resolution_pct: float) -> str:
    return ABSENT if math.isnan(resolution_pct) else f"± {resolution_pct:.1f} %"


def format_example(example: str, url: "str | None") -> str:
    """The name carries the assets link, so the table needs no column for it."""
    name = example.upper()
    return name if not url else f"[{name}]({url})"


def ref_url(repo_owner: str, ref: str) -> str:
    if re.match(r"^v\d+\.\d+\.\d+$", ref):
        return f"https://github.com/{repo_owner}/timefold-solver/releases/tag/{ref}"
    return f"https://github.com/{repo_owner}/timefold-solver/tree/{ref}"


def build_rows(expect: list, baseline_data: dict, sut_data: dict, asset_urls: dict) -> list:
    """One row for each example. Alphabetical: example names have no group/operation structure to
    sort by, unlike the move-provider report."""
    rows = []
    for example in sorted(expect):
        old, new = baseline_data.get(example), sut_data.get(example)
        delta_pct, verdict, resolution_pct = evaluate_row(old, new)
        rows.append((example, old, new, delta_pct, verdict, resolution_pct, asset_urls.get(example)))
    return rows


def render_table(rows: list) -> list:
    lines = ["| Example | Throughput | Resolution |",
             "|---|---:|---:|"]
    for example, old, new, delta_pct, verdict, resolution_pct, url in rows:
        lines.append("| {} | {} | {} |".format(
            format_example(example, url),
            format_throughput(old, new, delta_pct, verdict, resolution_pct),
            format_resolution(resolution_pct)))
    return lines


def render_legend() -> list:
    return ["",
            " · ".join(f"{v.emoji} {v.label}" for v in _ALL_VERDICTS)
            + f" · {UNRESOLVED} could not resolve a change this size",
            "",
            "#### Noise band",
            "",
            "- A speed is ops/s, old → new, with (new / old - 1) × 100 in brackets. "
            "Positive is faster.",
            f"- A delta inside its band counts as noise: ± {TOLERANCE_PCT:.0f} %.",
            "",
            "#### Resolution",
            "",
            "- The smallest difference this run could tell apart from no difference at all: "
            "the 99.9 % confidence interval on the delta itself.",
            "- It is paired. The two sides alternate fork by fork, so fork N of each ran about a "
            "minute apart and met the same machine; comparing them within the pair cancels "
            "whatever the machine was doing in between. Most of the remaining spread is a speed "
            "offset drawn once per JVM, which only more forks can average away.",
            "- It shrinks with the square root of the fork count, so it is also the answer to "
            "\"how many forks do we need?\".",
            "",
            "#### What the marks mean",
            "",
            f"- {UNDETERMINED.emoji} {UNDETERMINED.label}: the delta is outside its band, "
            f"but the two confidence intervals overlap, so this run cannot say which side is faster. "
            f"It fails the build, the same as {REGRESSION.emoji} {REGRESSION.label}. "
            f"Read the resolution, then run it again with more forks.",
            f"- {UNRESOLVED}: the resolution is no better than the band itself, so this row could "
            f"not have caught a regression worth failing on. Its delta says nothing either way - "
            f"the run needs more forks, or the machine was too busy to measure on.",
            "",
            "#### Notes",
            "",
            "- An example name links to the one GitHub Actions artifact holding the JFR recordings "
            "and CPU/alloc flamegraphs and heatmaps, for both sides.",
            f"- Measured on `{RUNNER_LABEL}`."]


def render_report(rows: list, baseline_ref: str, branch_ref: str, owner: str) -> tuple[str, int]:
    """The rendering half of build_report, so the selftest can drive it without a data directory."""
    counts = {}
    for _, _, _, _, verdict, _, _ in rows:
        counts[verdict] = counts.get(verdict, 0) + 1
    header = " · ".join(f"{v.emoji} {v.label} {counts[v]}" for v in _ALL_VERDICTS if v in counts)

    lines = [f"### {header}", "", f"_Old_: [TimefoldAI's {baseline_ref}]({ref_url('TimefoldAI', baseline_ref)})  ",
              f"_New_: [{owner}'s {branch_ref}]({ref_url(owner, branch_ref)})", ""]
    lines.extend(render_table(rows))
    lines.extend(render_legend())

    failing = any(verdict in _FAILING_VERDICTS for _, _, _, _, verdict, _, _ in rows)
    return "\n".join(lines), 1 if failing else 0


def build_report(data_dir: str, expect: list, baseline_ref: str, branch_ref: str, owner: str) -> tuple[str, int]:
    baseline_data, sut_data, asset_urls = load_all(data_dir)
    rows = build_rows(expect, baseline_data, sut_data, asset_urls)
    return render_report(rows, baseline_ref, branch_ref, owner)


def _selftest() -> None:
    def side(score, error, lo, hi, forks=()):
        return {"score": score, "score_error": error, "conf_lo": lo, "conf_hi": hi,
                "forks": list(forks)}

    fast = side(100.0, 1.0, 99.0, 101.0)
    slow = side(80.0, 1.0, 79.0, 81.0)
    same = side(100.5, 1.0, 99.5, 101.5)
    noisy = side(90.0, 0.0, math.nan, math.nan)

    # Resolution is paired: drift shared by both sides cancels, so a pair of sides that move
    # together resolves far better than either side's own spread suggests.
    drifting_old = side(100.0, 0.0, math.nan, math.nan, [90, 95, 100, 105, 110])
    drifting_new = side(100.0, 0.0, math.nan, math.nan, [90, 95, 100, 105, 110])
    assert resolution(drifting_old, drifting_new) == 0.0, "identical sides resolve perfectly"
    scattered_old = side(100.0, 0.0, math.nan, math.nan, [100, 100, 100, 100, 100])
    scattered_new = side(100.0, 0.0, math.nan, math.nan, [90, 95, 100, 105, 110])
    assert resolution(scattered_old, scattered_new) > 10.0, "unpaired scatter is not hidden"
    # Anything that cannot be paired resolves to nothing rather than to a wrong number.
    assert math.isnan(resolution(fast, slow)), "no rawData, no resolution"
    assert math.isnan(resolution(drifting_old, side(100.0, 0.0, 0.0, 0.0, [100, 100])))
    assert math.isnan(resolution(side(1.0, 0.0, 0.0, 0.0, [0.0, 1.0, 2.0]), drifting_old))

    assert is_unresolved(TOLERANCE_PCT + 0.1) and not is_unresolved(TOLERANCE_PCT - 0.1)
    assert not is_unresolved(math.nan), "unknown resolution must not raise the flag"

    # Regression: new (slow) is strictly below old (fast).
    delta, verdict, _ = evaluate_row(fast, slow)
    assert verdict == REGRESSION, verdict
    assert delta < 0

    # Improvement: new (fast) is strictly above old (slow).
    delta, verdict, _ = evaluate_row(slow, fast)
    assert verdict == IMPROVEMENT, verdict
    assert delta > 0

    # Within tolerance: old vs. a slightly higher score, well inside the band.
    delta, verdict, _ = evaluate_row(fast, same)
    assert verdict == TOLERANCE, verdict

    # NaN confidence interval outside tolerance: can't prove improvement or regression.
    delta, verdict, _ = evaluate_row(fast, noisy)
    assert verdict == UNDETERMINED, verdict

    # Missing side never crashes and is reported distinctly.
    delta, verdict, resolution_pct = evaluate_row(None, fast)
    assert verdict == MISSING and delta is None and math.isnan(resolution_pct)

    # A coarse resolution only annotates; it must not override the verdict.
    coarse_old = side(100.0, 0.0, math.nan, math.nan, [100, 100, 100, 100, 100])
    coarse_new = side(100.0, 0.0, math.nan, math.nan, [80, 90, 100, 110, 120])
    delta, verdict, resolution_pct = evaluate_row(coarse_old, coarse_new)
    assert verdict == TOLERANCE and is_unresolved(resolution_pct), resolution_pct

    assert format_resolution(math.nan) == ABSENT
    assert format_resolution(1.34) == "± 1.3 %"

    # Row order: alphabetical, from a deliberately scrambled input.
    expect = ["vehicle_routing", "cloud_balancing", "examination"]
    baseline = {name: fast for name in expect}
    sut = dict(baseline)
    sut["examination"] = slow
    rows = build_rows(expect, baseline, sut, {})
    assert [r[0] for r in rows] == sorted(expect)

    report, exit_code = render_report(rows, "v1.0.0", "main", "TimefoldAI")
    assert exit_code == 1, "a regression must fail the build"
    assert "| Example | Throughput | Resolution |" in report
    assert report.count("| Example |") == 1

    # The legend is sectioned.
    for heading in ("#### Noise band", "#### Resolution", "#### What the marks mean", "#### Notes"):
        assert heading in report, heading
    for mark in (f"{UNDETERMINED.emoji} {UNDETERMINED.label}:", f"{UNRESOLVED}:"):
        assert f"- {mark}" in report, mark

    # A missing side renders as a distinct row rather than vanishing.
    missing_rows = build_rows(["cloud_balancing", "examination"], {"cloud_balancing": fast}, {"cloud_balancing": fast}, {})
    missing_report, missing_exit = render_report(missing_rows, "v1.0.0", "main", "TimefoldAI")
    assert missing_exit == 1
    assert f"{MISSING.emoji} {ABSENT} → {ABSENT}" in missing_report

    # load_side reads whichever *Example param is present, case-insensitively, and works for either
    # benchmark class's param name.
    for param_name in ("csExample", "csJustifiedExample"):
        params = {param_name: "CLOUD_BALANCING"}
        example_key = next(key for key in params if key.endswith("Example"))
        assert params[example_key].lower() == "cloud_balancing", param_name

    print("summarize-scoredirector.py: selftest OK")


def main() -> None:
    if "--selftest" in sys.argv:
        _selftest()
        return

    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("data_dir", help="Directory holding one subdirectory per data-<example> artifact")
    parser.add_argument("--expect", required=True, help="JSON array of expected example names, e.g. [\"cloud_balancing\"]")
    parser.add_argument("--baseline", required=True)
    parser.add_argument("--branch", required=True)
    parser.add_argument("--owner", required=True)
    parser.add_argument("--selftest", action="store_true", help=argparse.SUPPRESS)
    args = parser.parse_args()

    report, exit_code = build_report(args.data_dir, json.loads(args.expect), args.baseline, args.branch, args.owner)
    print(report)
    sys.exit(exit_code)


if __name__ == "__main__":
    main()
