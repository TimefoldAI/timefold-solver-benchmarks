#!/usr/bin/env python3
"""Aggregate the score-director benchmark subjobs into one regression table.

Each CI subjob (one per example) uploads a "data-<example>" artifact holding
"baseline-results.json" and "sut-results.json" - the raw JMH JSON output for that one example. This
script joins every such pair on example, computes the speed difference, marks each row that
regresses or improves, and prints one markdown table plus a legend. Its exit code is the CI verdict:
0 only if every expected row is present and either within tolerance or an improvement.

Usage:
  python3 summarize-scoredirector.py DATA_DIR --expect JSON_ARRAY --baseline REF --branch REF
                                     --owner OWNER
  python3 summarize-scoredirector.py --selftest
"""
import argparse
import collections
import glob
import json
import math
import os
import re
import sys

# The band inside which a delta counts as noise. Two sides run on the same self-hosted machine, one
# after the other, so the noise of their difference is only a little more than one side's own error
# - unlike a shared runner, there is no separate JVM/JIT-shape lottery to absorb.
TOLERANCE_PCT = 3.0
# A row is marked with HIGH_ERROR when one side's own error is more than the band divided by
# sqrt(2) - that is, when the band above no longer covers this example.
HIGH_ERROR_BAND_FRACTION = math.sqrt(2)

RUNNER_LABEL = "self-hosted"

HIGH_ERROR = "⚠️"
ABSENT = "—"

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


def evaluate_row(old: "dict | None", new: "dict | None") -> tuple["float | None", Verdict, bool]:
    """Returns (delta_pct, verdict, high_error). delta_pct is None only for MISSING."""
    if old is None or new is None:
        return None, MISSING, False
    delta_pct = (new["score"] / old["score"] - 1) * 100
    error_limit = TOLERANCE_PCT / 100 / HIGH_ERROR_BAND_FRACTION
    high_error = relative_error(old) > error_limit or relative_error(new) > error_limit
    if abs(delta_pct) <= TOLERANCE_PCT:
        verdict = TOLERANCE
    elif new["conf_lo"] > old["conf_hi"]:
        verdict = IMPROVEMENT
    elif old["conf_lo"] > new["conf_hi"]:
        verdict = REGRESSION
    else:
        verdict = UNDETERMINED
    return delta_pct, verdict, high_error


def format_count(value: float) -> str:
    """Thin-space thousands separators, so the numbers stay readable in a narrow cell."""
    return format(round(value), ",").replace(",", " ")


def format_throughput(old: "dict | None", new: "dict | None", delta_pct: "float | None",
                       verdict: Verdict, high_error: bool) -> str:
    marker = verdict.emoji + (" " + HIGH_ERROR if high_error else "")
    if old is None or new is None:
        return f"{marker} {ABSENT} → {ABSENT}"
    return f"{marker} {format_count(old['score'])} → {format_count(new['score'])} ({delta_pct:+.1f} %)"


def format_margin(side: "dict | None") -> str:
    if side is None:
        return ABSENT
    value = relative_error(side)
    return ABSENT if math.isnan(value) else f"± {value * 100:.1f} %"


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
        delta_pct, verdict, high_error = evaluate_row(old, new)
        rows.append((example, old, new, delta_pct, verdict, high_error, asset_urls.get(example)))
    return rows


def render_table(rows: list) -> list:
    lines = ["| Example | Throughput | Old ± | New ± |",
             "|---|---:|---:|---:|"]
    for example, old, new, delta_pct, verdict, high_error, url in rows:
        lines.append("| {} | {} | {} | {} |".format(
            format_example(example, url),
            format_throughput(old, new, delta_pct, verdict, high_error),
            format_margin(old),
            format_margin(new)))
    return lines


def render_legend() -> list:
    return ["",
            " · ".join(f"{v.emoji} {v.label}" for v in _ALL_VERDICTS)
            + f" · {HIGH_ERROR} score error too big for its band",
            "",
            "#### Noise band",
            "",
            "- A speed is ops/s, old → new, with (new / old - 1) × 100 in brackets. "
            "Positive is faster.",
            f"- A delta inside its band counts as noise: ± {TOLERANCE_PCT:.0f} %.",
            "",
            "#### What the marks mean",
            "",
            f"- {UNDETERMINED.emoji} {UNDETERMINED.label}: the delta is outside its band, "
            f"but the two confidence intervals overlap, so this run cannot say which side is faster. "
            f"It fails the build, the same as {REGRESSION.emoji} {REGRESSION.label}. "
            f"Read the Old ±/New ± columns, then run it again with more forks.",
            f"- {HIGH_ERROR}: one side's own error is more than its band allows for. "
            f"The band is a fixed number from an earlier run, "
            f"so this says that run no longer describes this example.",
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
    fast = {"score": 100.0, "score_error": 1.0, "conf_lo": 99.0, "conf_hi": 101.0}
    slow = {"score": 80.0, "score_error": 1.0, "conf_lo": 79.0, "conf_hi": 81.0}
    same = {"score": 100.5, "score_error": 1.0, "conf_lo": 99.5, "conf_hi": 101.5}
    noisy = {"score": 90.0, "score_error": 0.0, "conf_lo": math.nan, "conf_hi": math.nan}

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
    delta, verdict, high_error = evaluate_row(None, fast)
    assert verdict == MISSING and delta is None and high_error is False

    # High relative error only annotates; it must not override a tolerance/regression verdict.
    high_err_side = {"score": 100.0, "score_error": 5.0, "conf_lo": 90.0, "conf_hi": 110.0}
    delta, verdict, high_error = evaluate_row(fast, high_err_side)
    assert verdict == TOLERANCE and high_error is True

    # A margin on a zero score is no percentage at all; it prints as absent rather than crashing.
    zero = {"score": 0.0, "score_error": 1.0, "conf_lo": math.nan, "conf_hi": math.nan}
    assert format_margin(zero) == ABSENT
    assert format_margin(None) == ABSENT
    assert format_margin(fast) == "± 1.0 %"

    # Row order: alphabetical, from a deliberately scrambled input.
    expect = ["vehicle_routing", "cloud_balancing", "examination"]
    baseline = {name: fast for name in expect}
    sut = dict(baseline)
    sut["examination"] = slow
    rows = build_rows(expect, baseline, sut, {})
    assert [r[0] for r in rows] == sorted(expect)

    report, exit_code = render_report(rows, "v1.0.0", "main", "TimefoldAI")
    assert exit_code == 1, "a regression must fail the build"
    assert "| Example | Throughput | Old ± | New ± |" in report
    assert report.count("| Example |") == 1

    # The legend is sectioned.
    for heading in ("#### Noise band", "#### What the marks mean", "#### Notes"):
        assert heading in report, heading
    for mark in (f"{UNDETERMINED.emoji} {UNDETERMINED.label}:", f"{HIGH_ERROR}:"):
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
