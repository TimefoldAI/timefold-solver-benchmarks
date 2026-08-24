#!/usr/bin/env python3
"""Aggregate the move-provider benchmark subjobs into one regression table.

Each CI subjob (one per move provider) uploads a "data-<provider>" artifact holding
"baseline-results.json" and "sut-results.json" - the raw JMH JSON output for that one provider,
both scenarios (fewStepsManyMoves, manyStepsFewMoves). This script joins every such pair on
(provider, scenario), computes the speed difference, marks each row that regresses or improves,
and prints one markdown table plus a legend. Its exit code is the CI verdict: 0 only if every
expected row is present and either within tolerance or an improvement.

Usage:
  python3 summarize-moveprovider.py DATA_DIR --expect JSON_ARRAY --baseline REF --branch REF
                                    --owner OWNER
  python3 summarize-moveprovider.py --selftest
"""
import argparse
import glob
import json
import math
import os
import re
import sys

SCENARIOS = ("singleDraw", "manyDraws")
TOLERANCE_PCT = 3.0
HIGH_ERROR_THRESHOLD = 0.02
RUNNER_LABEL = "ubuntu-24.04-arm"

REGRESSION = "‼️ regression"
UNDETERMINED = "⁉️ undetermined"
IMPROVEMENT = "🚀 improvement"
TOLERANCE = "✅ within tolerance"
MISSING = "❌ missing data"

_FAILING_VERDICTS = {REGRESSION, UNDETERMINED, MISSING}


def _to_float(value) -> float:
    """JMH writes NaN both as the bare literal and as the string "NaN"; either parses cleanly."""
    return float(value)


def load_side(path: str) -> dict:
    """Reads one results.json, keyed by (provider_key, scenario). provider_key is e.g.

    "basic:change" or "list:sub_list_swap", derived from whichever *MoveProvider param is present.
    """
    with open(path) as f:
        entries = json.load(f)
    result = {}
    for entry in entries:
        scenario = entry["benchmark"].rsplit(".", 1)[-1]
        params = entry["params"]
        if "basicMoveProvider" in params:
            provider_key = "basic:" + params["basicMoveProvider"].lower()
        else:
            provider_key = "list:" + params["listMoveProvider"].lower()
        metric = entry["primaryMetric"]
        conf = metric["scoreConfidence"]
        result[(provider_key, scenario)] = {
            "score": _to_float(metric["score"]),
            "score_error": _to_float(metric["scoreError"]),
            "conf_lo": _to_float(conf[0]),
            "conf_hi": _to_float(conf[1]),
        }
    return result


def load_all(data_dir: str) -> tuple[dict, dict]:
    baseline, sut = {}, {}
    for baseline_path in glob.glob(os.path.join(data_dir, "*", "baseline-results.json")):
        sut_path = os.path.join(os.path.dirname(baseline_path), "sut-results.json")
        baseline.update(load_side(baseline_path))
        if os.path.exists(sut_path):
            sut.update(load_side(sut_path))
    return baseline, sut


def relative_error(side: dict) -> float:
    if side["score"] == 0:
        return math.nan
    return abs(side["score_error"] / side["score"])


def evaluate_row(old: "dict | None", new: "dict | None") -> tuple["float | None", str, bool]:
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


def format_side(side: "dict | None") -> str:
    if side is None:
        return "—"
    rel_err = relative_error(side)
    formatted_score = format(round(side["score"]), ",").replace(",", " ")
    if math.isnan(rel_err):
        return f"{formatted_score}"
    return f"{formatted_score} ± {rel_err * 100:.2f} %"


def ref_url(repo_owner: str, ref: str) -> str:
    if re.match(r"^v\d+\.\d+\.\d+$", ref):
        return f"https://github.com/{repo_owner}/timefold-solver/releases/tag/{ref}"
    return f"https://github.com/{repo_owner}/timefold-solver/tree/{ref}"


def build_report(data_dir: str, expect: list, baseline_ref: str, branch_ref: str, owner: str) -> tuple[str, int]:
    baseline_data, sut_data = load_all(data_dir)
    rows = []
    for provider_key in expect:
        for scenario in SCENARIOS:
            key = (provider_key, scenario)
            old, new = baseline_data.get(key), sut_data.get(key)
            delta_pct, verdict, high_error = evaluate_row(old, new)
            rows.append((provider_key, scenario, old, new, delta_pct, verdict, high_error))

    counts = {}
    for row in rows:
        counts[row[5]] = counts.get(row[5], 0) + 1
    header = " · ".join(f"{v} {counts[v]}" for v in
                         (REGRESSION, UNDETERMINED, MISSING, IMPROVEMENT, TOLERANCE) if v in counts)

    # Move provider first, then benchmark scenario in its declared (not alphabetical) order.
    rows.sort(key=lambda r: (r[0], SCENARIOS.index(r[1])))

    lines = [f"### {header}", ""]
    lines.append(f"_Old_: [TimefoldAI's {baseline_ref}]({ref_url('TimefoldAI', baseline_ref)})  ")
    lines.append(f"_New_: [{owner}'s {branch_ref}]({ref_url(owner, branch_ref)})")
    lines.append("")
    lines.append("| Move provider | Scenario | Old (ops/s) | New (ops/s) | Δ | Verdict |")
    lines.append("|---|---|---:|---:|---:|---|")
    for provider_key, scenario, old, new, delta_pct, verdict, high_error in rows:
        provider_name = provider_key.split(":", 1)[1].upper()
        delta_str = "—" if delta_pct is None else f"{delta_pct:+.1f} %"
        verdict_str = verdict + (" ⚠️" if high_error else "")
        lines.append(f"| {provider_name} | {scenario} | {format_side(old)} | {format_side(new)} "
                      f"| {delta_str} | {verdict_str} |")

    lines.append("")
    lines.append("Δ is (new / old - 1) × 100. Positive is faster, negative is a slowdown.")
    lines.append(f"Δ within ± {TOLERANCE_PCT:.0f} % is treated as noise. ± after a speed is a 99.9 % confidence interval.")
    lines.append(f"⚠️ marks a relative score error over ± {HIGH_ERROR_THRESHOLD * 100:.0f} % (annotation only, does not change the verdict).")
    lines.append(f"Measured on `{RUNNER_LABEL}` runners.")

    exit_code = 1 if any(row[5] in _FAILING_VERDICTS for row in rows) else 0
    return "\n".join(lines), exit_code


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
    high_err_side = {"score": 100.0, "score_error": 5.0, "conf_lo": 90.0, "conf_hi": 110.0}
    delta, verdict, high_error = evaluate_row(fast, high_err_side)
    assert verdict == TOLERANCE and high_error is True

    print("summarize-moveprovider.py: selftest OK")


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
