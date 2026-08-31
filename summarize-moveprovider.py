#!/usr/bin/env python3
"""Aggregate the move-provider benchmark subjobs into one regression table.

Each CI subjob (one per move provider) uploads a "data-<provider>" artifact holding
"baseline-results.json" and "sut-results.json" - the raw JMH JSON output for that one provider,
both scenarios. This script joins every such pair on (provider, scenario), computes the speed
difference, marks each row that regresses or improves, and prints one markdown table per variable
family plus a legend. Its exit code is the CI verdict: 0 only if every expected row is present and
either within tolerance or an improvement.

The two scenarios split the two costs a move provider carries:

  commitMove - draw one move, execute it, settle the neighborhood network, then undo it. Its speed
               is the cost to APPLY a move. It calculates no score, so it is not a whole step.
  drawOnly   - draw many moves from one settled solution and commit none. Its speed is the cost to
               MAKE a move, with no commit in it at all.

Absolute throughput is not comparable between two move providers, because one move of a mass
provider does the work of ten moves of a single-value provider. So each row also carries the mean
number of values its moves change - read from the "movedValues" aux counter the benchmark records -
and a "ns/value" column, which is the column to rank when deciding what to optimize.

Usage:
  python3 summarize-moveprovider.py DATA_DIR --expect JSON_ARRAY --baseline REF --branch REF
                                    --owner OWNER
  python3 summarize-moveprovider.py --selftest
"""
import argparse
import collections
import glob
import json
import math
import os
import re
import sys

COMMIT_MOVE = "commitMove"
DRAW_ONLY = "drawOnly"
SCENARIOS = (COMMIT_MOVE, DRAW_ONLY)

# The baseline jar is built from whatever benchmarks ref matches the baseline solver ref, so it may
# still carry the old scenario name. "singleDraw" and "commitMove" are the same measurement; the
# rename only dropped an unused draw-count parameter. "manyDraws" needs no entry: it has no
# counterpart, and an unknown scenario key is never looked up, so it drops out on its own.
SCENARIO_ALIASES = {"singleDraw": COMMIT_MOVE}

TOLERANCE_PCT = 3.0
HIGH_ERROR_THRESHOLD = 0.02
WORKLOAD_TOLERANCE_PCT = 3.0
WORST_COUNT = 3
RUNNER_LABEL = "ubuntu-24.04-arm"
# Mirrors AbstractMoveProviderBenchmark.DRAW_ONLY_DRAWS. Only ever printed, in the legend; the
# metrics need no divisor, because @OperationsPerInvocation already makes the reported unit one draw.
DRAW_ONLY_DRAWS = 500

HIGH_ERROR = "⚠️"
WORKLOAD_CHANGED = "⚖️"
ABSENT = "—"

# The counter is recorded as an AuxCounters.Type.OPERATIONS rate, in the same unit as the primary
# metric, so the values for each operation is the quotient of the two.
MOVED_VALUES_KEY = "movedValues"

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


def read_moved_values(entry: dict, score: float) -> "float | None":
    """Values changed for each operation, or None when this result predates the counter.

    The aux counter is a rate in the primary metric's unit, so dividing the two cancels the time
    base and leaves a plain count - no knowledge of iteration count or duration required.
    """
    secondary = entry.get("secondaryMetrics", {}).get(MOVED_VALUES_KEY)
    if secondary is None or score == 0:
        return None
    counter_score = _to_float(secondary["score"])
    if math.isnan(counter_score):
        return None
    return counter_score / score


def load_side(path: str) -> dict:
    """Reads one results.json, keyed by (provider_key, scenario). provider_key is e.g.

    "basic:change" or "list:sub_list_swap", derived from whichever *MoveProvider param is present.
    """
    with open(path) as f:
        entries = json.load(f)
    result = {}
    for entry in entries:
        scenario = entry["benchmark"].rsplit(".", 1)[-1]
        scenario = SCENARIO_ALIASES.get(scenario, scenario)
        params = entry["params"]
        if "basicMoveProvider" in params:
            provider_key = "basic:" + params["basicMoveProvider"].lower()
        else:
            provider_key = "list:" + params["listMoveProvider"].lower()
        metric = entry["primaryMetric"]
        conf = metric["scoreConfidence"]
        score = _to_float(metric["score"])
        result[(provider_key, scenario)] = {
            "score": score,
            "score_error": _to_float(metric["scoreError"]),
            "conf_lo": _to_float(conf[0]),
            "conf_hi": _to_float(conf[1]),
            "moved_values": read_moved_values(entry, score),
        }
    return result


def load_all(data_dir: str) -> tuple[dict, dict, dict]:
    """The third dict maps provider_key -> assets artifact URL (one per provider subjob, covering
    both scenarios and both baseline and SUT - see "Archive benchmark assets" in the workflow)."""
    baseline, sut, asset_urls = {}, {}, {}
    for baseline_path in glob.glob(os.path.join(data_dir, "*", "baseline-results.json")):
        provider_dir = os.path.dirname(baseline_path)
        sut_path = os.path.join(provider_dir, "sut-results.json")
        baseline_side = load_side(baseline_path)
        baseline.update(baseline_side)
        if os.path.exists(sut_path):
            sut.update(load_side(sut_path))
        url_path = os.path.join(provider_dir, "assets-url.txt")
        if os.path.exists(url_path):
            url = open(url_path).read().strip()
            if url:
                for provider_key in {key[0] for key in baseline_side}:
                    asset_urls[provider_key] = url
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
    high_error = relative_error(old) > HIGH_ERROR_THRESHOLD or relative_error(new) > HIGH_ERROR_THRESHOLD
    if abs(delta_pct) <= TOLERANCE_PCT:
        verdict = TOLERANCE
    elif new["conf_lo"] > old["conf_hi"]:
        verdict = IMPROVEMENT
    elif old["conf_lo"] > new["conf_hi"]:
        verdict = REGRESSION
    else:
        verdict = UNDETERMINED
    return delta_pct, verdict, high_error


def ns_per_value(side: "dict | None") -> "float | None":
    """Nanoseconds spent for each value the drawn moves change."""
    if side is None or not side.get("moved_values"):
        return None
    return 1e9 / (side["score"] * side["moved_values"])


def workload_changed(old: "dict | None", new: "dict | None") -> bool:
    """True when the two sides do not do the same quantity of work, which makes their delta
    incomparable. Only decidable when both sides recorded the counter."""
    if old is None or new is None:
        return False
    old_values, new_values = old.get("moved_values"), new.get("moved_values")
    if not old_values or not new_values:
        return False
    return abs(new_values / old_values - 1) * 100 > WORKLOAD_TOLERANCE_PCT


def format_count(value: float) -> str:
    """Thin-space thousands separators, so the numbers stay readable in a narrow cell."""
    return format(round(value), ",").replace(",", " ")


def format_scenario(old: "dict | None", new: "dict | None", delta_pct: "float | None",
                     verdict: Verdict, high_error: bool) -> str:
    marker = verdict.emoji + (" " + HIGH_ERROR if high_error else "")
    if old is None or new is None:
        return f"{marker} {ABSENT} → {ABSENT}"
    return f"{marker} {format_count(old['score'])} → {format_count(new['score'])} ({delta_pct:+.1f} %)"


def format_values_per_move(old: "dict | None", new: "dict | None") -> str:
    """The commitMove count, which draws exactly one move and so is already per move.

    drawOnly reports the same quantity, because @OperationsPerInvocation makes its unit one drawn
    move too; the column reads only one of the two.
    """
    old_values = old.get("moved_values") if old else None
    new_values = new.get("moved_values") if new else None
    if workload_changed(old, new):
        return f"{old_values:.1f} → {new_values:.1f} {WORKLOAD_CHANGED}"
    shown = new_values if new_values is not None else old_values
    return ABSENT if shown is None else f"{shown:.1f}"


def format_ns_per_value(side: "dict | None") -> str:
    value = ns_per_value(side)
    return ABSENT if value is None else format_count(value)


def format_provider(provider_key: str, url: "str | None") -> str:
    """The name carries the assets link, so the table needs no column for it."""
    name = provider_key.split(":", 1)[1].upper()
    return name if not url else f"[{name}]({url})"


def ref_url(repo_owner: str, ref: str) -> str:
    if re.match(r"^v\d+\.\d+\.\d+$", ref):
        return f"https://github.com/{repo_owner}/timefold-solver/releases/tag/{ref}"
    return f"https://github.com/{repo_owner}/timefold-solver/tree/{ref}"


def build_rows(expect: list, baseline_data: dict, sut_data: dict, asset_urls: dict) -> list:
    """One row for each provider, holding both scenarios; the scenarios are columns, not rows."""
    rows = []
    for provider_key in expect:
        scenarios = {}
        for scenario in SCENARIOS:
            key = (provider_key, scenario)
            old, new = baseline_data.get(key), sut_data.get(key)
            delta_pct, verdict, high_error = evaluate_row(old, new)
            scenarios[scenario] = (old, new, delta_pct, verdict, high_error)
        rows.append((provider_key, scenarios, asset_urls.get(provider_key)))
    return rows


def worst_line(rows: list, scenario: str, label: str) -> "str | None":
    """The providers most expensive for each value they move - the ones worth investigating.

    One line for each scenario: from drawOnly it ranks the cost to generate a value, from commitMove
    the cost to commit one.
    """
    ranked = []
    for provider_key, scenarios, _ in rows:
        value = ns_per_value(scenarios[scenario][1])
        if value is not None:
            ranked.append((value, provider_key))
    if not ranked:
        return None
    ranked.sort(reverse=True)
    shown = [f"{provider_key.split(':', 1)[1].upper()} {format_count(value)}"
             for value, provider_key in ranked[:WORST_COUNT]]
    return f"{label}: " + " · ".join(shown)


def error_notes(rows: list) -> list:
    """One line for each cell the table marked with a high score error, naming both margins.

    The cell has no room for the margin itself, so the reader cannot tell whether its delta is
    bigger than the noise it sits in. high_error is only ever True when both sides are present.
    """
    def as_pct(side: dict) -> str:
        value = relative_error(side)
        return ABSENT if math.isnan(value) else f"± {value * 100:.1f} %"

    lines = []
    for provider_key, scenarios, _ in rows:
        for scenario in SCENARIOS:
            old, new, delta_pct, _, high_error = scenarios[scenario]
            if high_error:
                lines.append(f"{HIGH_ERROR} {provider_key.split(':', 1)[1].upper()} `{scenario}`: "
                             f"{as_pct(old)} old · {as_pct(new)} new, against a "
                             f"{delta_pct:+.1f} % delta")
    return lines


def render_table(rows: list) -> list:
    lines = [f"| Move provider | {COMMIT_MOVE} | {DRAW_ONLY} | Values/move | ns/value |",
             "|---|---:|---:|---:|---:|"]
    for provider_key, scenarios, url in rows:
        commit, draw = scenarios[COMMIT_MOVE], scenarios[DRAW_ONLY]
        lines.append("| {} | {} | {} | {} | {} |".format(
            format_provider(provider_key, url),
            format_scenario(*commit),
            format_scenario(*draw),
            format_values_per_move(commit[0], commit[1]),
            format_ns_per_value(draw[1])))
    return lines


def build_report(data_dir: str, expect: list, baseline_ref: str, branch_ref: str, owner: str) -> tuple[str, int]:
    baseline_data, sut_data, asset_urls = load_all(data_dir)
    rows = build_rows(expect, baseline_data, sut_data, asset_urls)
    return render_report(rows, baseline_ref, branch_ref, owner)


def _selftest() -> None:
    fast = {"score": 100.0, "score_error": 1.0, "conf_lo": 99.0, "conf_hi": 101.0, "moved_values": 2.0}
    slow = {"score": 80.0, "score_error": 1.0, "conf_lo": 79.0, "conf_hi": 81.0, "moved_values": 2.0}
    same = {"score": 100.5, "score_error": 1.0, "conf_lo": 99.5, "conf_hi": 101.5, "moved_values": 2.0}
    noisy = {"score": 90.0, "score_error": 0.0, "conf_lo": math.nan, "conf_hi": math.nan, "moved_values": 2.0}

    # Regression: new (slow) is strictly below old (fast).
    delta, verdict, _ = evaluate_row(fast, slow)
    assert verdict == REGRESSION, verdict
    assert delta < 0

    # Improvement: new (fast) is strictly above old (slow).
    delta, verdict, _ = evaluate_row(slow, fast)
    assert verdict == IMPROVEMENT, verdict
    assert delta > 0

    # Within tolerance: old vs. a slightly higher score, well inside +/-3%.
    delta, verdict, _ = evaluate_row(fast, same)
    assert verdict == TOLERANCE, verdict

    # NaN confidence interval outside tolerance: can't prove improvement or regression.
    delta, verdict, _ = evaluate_row(fast, noisy)
    assert verdict == UNDETERMINED, verdict

    # Missing side never crashes and is reported distinctly.
    delta, verdict, high_error = evaluate_row(None, fast)
    assert verdict == MISSING and delta is None and high_error is False

    # High relative error only annotates; it must not override a tolerance/regression verdict.
    high_err_side = {"score": 100.0, "score_error": 5.0, "conf_lo": 90.0, "conf_hi": 110.0, "moved_values": 2.0}
    delta, verdict, high_error = evaluate_row(fast, high_err_side)
    assert verdict == TOLERANCE and high_error is True

    # The counter is a rate in the primary unit, so values/op is the quotient of the two scores.
    entry = {"secondaryMetrics": {MOVED_VALUES_KEY: {"score": 300.0}}}
    assert read_moved_values(entry, 100.0) == 3.0
    assert read_moved_values({"secondaryMetrics": {}}, 100.0) is None
    assert read_moved_values(entry, 0.0) is None
    assert read_moved_values({"secondaryMetrics": {MOVED_VALUES_KEY: {"score": "NaN"}}}, 100.0) is None

    # ns/value: 100 ops/s moving 2 values each is 1e9 / 200 ns for each value.
    assert ns_per_value(fast) == 1e9 / 200
    assert ns_per_value(None) is None
    assert ns_per_value({"score": 100.0, "moved_values": None}) is None
    assert ns_per_value({"score": 100.0, "moved_values": 0.0}) is None

    # The workload guard needs both sides, and ignores a difference within tolerance.
    heavier = dict(fast, moved_values=4.0)
    barely = dict(fast, moved_values=2.04)
    assert workload_changed(fast, heavier) is True
    assert workload_changed(fast, barely) is False
    assert workload_changed(fast, fast) is False
    assert workload_changed(fast, dict(fast, moved_values=None)) is False
    assert workload_changed(None, fast) is False
    assert WORKLOAD_CHANGED in format_values_per_move(fast, heavier)
    assert format_values_per_move(fast, fast) == "2.0"
    assert format_values_per_move(None, None) == ABSENT

    # A workload change annotates only; it must never fail the build.
    assert WORKLOAD_CHANGED not in {v.emoji for v in _FAILING_VERDICTS}

    # One row for each provider, split into the two families, and either scenario can fail the build.
    baseline = {("basic:change", COMMIT_MOVE): fast, ("basic:change", DRAW_ONLY): fast,
                ("list:list_change", COMMIT_MOVE): fast, ("list:list_change", DRAW_ONLY): fast}
    sut = dict(baseline)
    sut[("list:list_change", DRAW_ONLY)] = slow
    expect = ["basic:change", "list:list_change"]
    rows = build_rows(expect, baseline, sut, {})
    assert len(rows) == len(expect)
    assert [r[0] for r in rows] == expect
    assert set(rows[0][1]) == set(SCENARIOS)
    report, exit_code = render_report(rows, "v1.0.0", "main", "TimefoldAI")
    assert exit_code == 1, "a regression in drawOnly alone must still fail"
    assert "#### Basic variable" in report and "#### List variable" in report
    assert report.count("| Move provider |") == 2

    # Both ranking lines, for both families.
    assert report.count("Highest generation ns/value:") == 2
    assert report.count("Highest commit ns/value:") == 2

    # The column explainer, with one row for each of the four columns.
    assert "#### What the columns mean" in report
    for column in (f"`{COMMIT_MOVE}`", f"`{DRAW_ONLY}`", "`Values/move`", "`ns/value`"):
        assert f"| {column} |" in report, column
    assert str(DRAW_ONLY_DRAWS) in report

    # A ⚠️ cell names both its margins below the table; a clean table prints no such line.
    assert error_notes(rows) == []
    assert f"{HIGH_ERROR} CHANGE" not in report
    noisy_sut = dict(sut)
    noisy_sut[("basic:change", COMMIT_MOVE)] = high_err_side
    noisy_rows = build_rows(expect, baseline, noisy_sut, {})
    notes = error_notes(noisy_rows)
    assert len(notes) == 1, notes
    assert notes[0] == f"{HIGH_ERROR} CHANGE `{COMMIT_MOVE}`: ± 1.0 % old · ± 5.0 % new, against a +0.0 % delta", notes[0]
    noisy_report, _ = render_report(noisy_rows, "v1.0.0", "main", "TimefoldAI")
    assert notes[0] in noisy_report
    # A margin on a zero score is no percentage at all; it prints as absent rather than crashing.
    zero = {"score": 0.0, "score_error": 1.0, "conf_lo": math.nan, "conf_hi": math.nan, "moved_values": 2.0}
    zero_rows = build_rows(["basic:change"],
                           {("basic:change", COMMIT_MOVE): high_err_side, ("basic:change", DRAW_ONLY): fast},
                           {("basic:change", COMMIT_MOVE): zero, ("basic:change", DRAW_ONLY): fast}, {})
    assert error_notes(zero_rows) == [
        f"{HIGH_ERROR} CHANGE `{COMMIT_MOVE}`: ± 5.0 % old · {ABSENT} new, against a -100.0 % delta"]

    # An old baseline jar still reports singleDraw; the alias joins it to commitMove.
    assert SCENARIO_ALIASES.get("singleDraw") == COMMIT_MOVE
    assert "manyDraws" not in SCENARIO_ALIASES, "manyDraws has no counterpart and must drop out"

    print("summarize-moveprovider.py: selftest OK")


def render_legend() -> list:
    """Everything after the last table: the emoji key, then an explainer for the four columns."""
    lines = ["",
             " · ".join(f"{v.emoji} {v.label}" for v in _ALL_VERDICTS)
             + f" · {HIGH_ERROR} score error above ± {HIGH_ERROR_THRESHOLD * 100:.0f} %"
             + f" · {WORKLOAD_CHANGED} workload changed",
             f"A speed is ops/s, old → new, with (new / old - 1) × 100 in brackets. Positive is "
             f"faster. Within ± {TOLERANCE_PCT:.0f} % is treated as noise.",
             "",
             "#### What the columns mean",
             "",
             "| Column | Unit | Measures | Rank it? |",
             "|---|---|---|---|"]
    columns = [
        (f"`{COMMIT_MOVE}`", "moves/s",
         "Draw one move, execute it, settle the neighborhood network, then undo it. Calculates no "
         "score, so it is not a whole step.",
         "No - mixes the move's own execute with shared settle machinery"),
        (f"`{DRAW_ONLY}`", "draws/s",
         f"Draw {DRAW_ONLY_DRAWS} moves from one settled solution with one iterator, and commit none.",
         "No - one move can name many values"),
        ("`Values/move`", "count",
         "Mean variable writes in one move. Explains why the two speeds differ between providers.",
         "—"),
        ("`ns/value`", "ns",
         f"`{DRAW_ONLY}` divided by `Values/move`: the cost to generate one moved value.",
         "**Yes** - this is the triage column"),
    ]
    lines.extend("| {} | {} | {} | {} |".format(*column) for column in columns)
    lines.append("")
    lines.append(f"The lines above each table rank both costs: generation from `{DRAW_ONLY}`, commit "
                 f"from `{COMMIT_MOVE}` divided by `Values/move`. Commit is the larger of the two.")
    lines.append("Do not compare `ns/value` between the two tables. They run on different data sets.")
    lines.append(f"`{DRAW_ONLY}` reuses one iterator for the whole invocation, as a production step "
                 "does. That iterator retires candidates that keep coming back empty, so its data "
                 "sets stay warm and its pool gets smaller as the invocation runs. Real generation "
                 "can be a little slower.")
    lines.append(f"{WORKLOAD_CHANGED} marks a provider whose quantity of work is not the same on both "
                 "sides. Its delta compares two different workloads, and is not reliable.")
    lines.append("A move provider name links to the one GitHub Actions artifact holding the JFR "
                 "recordings and CPU/alloc flamegraphs and heatmaps for both of its scenarios, old "
                 "and new alike.")
    lines.append(f"Measured on `{RUNNER_LABEL}` runners.")
    return lines


def render_report(rows: list, baseline_ref: str, branch_ref: str, owner: str) -> tuple[str, int]:
    """The rendering half of build_report, so the selftest can drive it without a data directory."""
    counts = {}
    for _, scenarios, _ in rows:
        for _, _, _, verdict, _ in scenarios.values():
            counts[verdict] = counts.get(verdict, 0) + 1
    header = " · ".join(f"{v.emoji} {v.label} {counts[v]}" for v in _ALL_VERDICTS if v in counts)

    lines = [f"### {header}", ""]
    lines.append(f"_Old_: [TimefoldAI's {baseline_ref}]({ref_url('TimefoldAI', baseline_ref)})  ")
    lines.append(f"_New_: [{owner}'s {branch_ref}]({ref_url(owner, branch_ref)})")

    for title, prefix in (("Basic variable", "basic:"), ("List variable", "list:")):
        family_rows = [row for row in rows if row[0].startswith(prefix)]
        if not family_rows:
            continue
        lines.append("")
        lines.append(f"#### {title}")
        lines.append("")
        ranking = [worst_line(family_rows, DRAW_ONLY, "Highest generation ns/value"),
                   worst_line(family_rows, COMMIT_MOVE, "Highest commit ns/value")]
        ranking = [line for line in ranking if line]
        if ranking:
            # Two spaces end a markdown line without ending the paragraph.
            lines.extend(f"{line}  " for line in ranking)
            lines.append("")
        lines.extend(render_table(family_rows))
        notes = error_notes(family_rows)
        if notes:
            lines.append("")
            # Two spaces end a markdown line without ending the paragraph.
            lines.extend(f"{note}  " for note in notes)

    lines.extend(render_legend())

    failing = any(verdict in _FAILING_VERDICTS
                  for _, scenarios, _ in rows
                  for _, _, _, verdict, _ in scenarios.values())
    return "\n".join(lines), 1 if failing else 0


def main() -> None:
    if "--selftest" in sys.argv:
        _selftest()
        return

    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("data_dir", help="Directory holding one subdirectory per data-<provider> artifact")
    parser.add_argument("--expect", required=True, help="JSON array of expected provider keys, e.g. [\"basic:change\"]")
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
