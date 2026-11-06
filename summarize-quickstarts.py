#!/usr/bin/env python3
"""Aggregate the quickstart benchmark subjobs into one regression table.

Each CI subjob (one per quickstart) uploads a "data-<quickstart>" artifact holding "baseline.csv"
and "sut.csv" - the output of benchmark-quickstarts.py for that one quickstart - and
"assets-url.txt". This script joins every pair on quickstart, computes the speed difference, marks
each row that regresses or improves, and prints one markdown table plus a legend. Its exit code is
the CI verdict: 0 only if every expected row is present and either within tolerance or an
improvement.

The verdict rules and formatting come from summarize-scoredirector.py, so both reports read alike.

Usage:
  python3 summarize-quickstarts.py DATA_DIR --expect JSON_ARRAY --baseline REF --branch REF
                                   --owner OWNER
  python3 summarize-quickstarts.py --selftest
"""
import argparse
import csv
import glob
import importlib.util
import json
import math
import os
import sys


def _load_sibling_module():
    """The file name has hyphens, so it cannot be imported by name."""
    path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "summarize-scoredirector.py")
    spec = importlib.util.spec_from_file_location("summarize_scoredirector", path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


_SD = _load_sibling_module()


def _to_float(value: str) -> float:
    return float(value) if value else math.nan


def load_side(path: str) -> dict:
    """Reads one CSV written by benchmark-quickstarts.py, keyed by quickstart name.

    A row without a positive mean or without confidence bounds (fewer than two runs) is left out,
    so it reports as missing instead of passing unchecked.
    """
    result = {}
    with open(path, newline="") as f:
        for row in csv.DictReader(f):
            score, conf_lo, conf_hi = (_to_float(row[key]) for key in ("mean", "ci_lower", "ci_upper"))
            if not score > 0 or math.isnan(conf_lo) or math.isnan(conf_hi):
                continue
            result[row["quickstart"]] = {"score": score, "conf_lo": conf_lo, "conf_hi": conf_hi, "forks": []}
    return result


def load_all(data_dir: str) -> tuple[dict, dict, dict]:
    """The third dict maps quickstart -> assets artifact URL (JFR recordings of both sides)."""
    baseline, sut, asset_urls = {}, {}, {}
    for baseline_path in glob.glob(os.path.join(data_dir, "*", "baseline.csv")):
        quickstart_dir = os.path.dirname(baseline_path)
        side = load_side(baseline_path)
        baseline.update(side)
        sut_path = os.path.join(quickstart_dir, "sut.csv")
        if os.path.exists(sut_path):
            sut.update(load_side(sut_path))
        url_path = os.path.join(quickstart_dir, "assets-url.txt")
        if os.path.exists(url_path):
            url = open(url_path).read().strip()
            if url:
                for quickstart in side:
                    asset_urls[quickstart] = url
    return baseline, sut, asset_urls


def relative_half_width(side: dict) -> float:
    return (side["conf_hi"] - side["conf_lo"]) / 2 / side["score"]


def resolution(old: dict, new: dict) -> float:
    """Half-width of the 99.9 % confidence interval on the delta, in percent. NaN when unknowable.

    Unpaired: all baseline runs happen before all SUT runs, so no run of one side has a partner
    on the other. The two relative half-widths add in quadrature.
    """
    return math.sqrt(relative_half_width(old) ** 2 + relative_half_width(new) ** 2) * 100


def evaluate_row(old: "dict | None", new: "dict | None"):
    delta_pct, verdict, _ = _SD.evaluate_row(old, new)
    if old is None or new is None:
        return delta_pct, verdict, math.nan
    return delta_pct, verdict, resolution(old, new)


def build_rows(expect: list, baseline_data: dict, sut_data: dict, asset_urls: dict) -> list:
    rows = []
    for quickstart in sorted(expect):
        old, new = baseline_data.get(quickstart), sut_data.get(quickstart)
        delta_pct, verdict, resolution_pct = evaluate_row(old, new)
        rows.append((quickstart, old, new, delta_pct, verdict, resolution_pct, asset_urls.get(quickstart)))
    return rows


def render_table(rows: list) -> list:
    lines = ["| Quickstart | Move evaluation speed | Resolution |",
             "|---|---:|---:|"]
    for quickstart, old, new, delta_pct, verdict, resolution_pct, url in rows:
        lines.append("| {} | {} | {} |".format(
            _SD.format_example(quickstart, url),
            _SD.format_throughput(old, new, delta_pct, verdict, resolution_pct),
            _SD.format_resolution(resolution_pct)))
    return lines


def render_legend() -> list:
    return ["",
            " · ".join(f"{v.emoji} {v.label}" for v in _SD._ALL_VERDICTS)
            + f" · {_SD.UNRESOLVED} could not resolve a change this size",
            "",
            "#### Noise band",
            "",
            "- A speed is the mean move evaluation speed in moves/s, old → new, "
            "with (new / old - 1) × 100 in brackets. Positive is faster.",
            f"- A delta inside its band counts as noise: ± {_SD.TOLERANCE_PCT:.0f} %.",
            "",
            "#### Resolution",
            "",
            "- The smallest difference this run could tell apart from no difference at all: "
            "the 99.9 % confidence interval on the delta itself.",
            "- It is unpaired. All runs of the old side happen before all runs of the new side, "
            "so whatever the machine did in between is not cancelled out.",
            "- It shrinks with the square root of the run count.",
            "",
            "#### What the marks mean",
            "",
            f"- {_SD.UNDETERMINED.emoji} {_SD.UNDETERMINED.label}: the delta is outside its band, "
            f"but the two confidence intervals overlap, so this run cannot say which side is faster. "
            f"It fails the build, the same as {_SD.REGRESSION.emoji} {_SD.REGRESSION.label}. "
            f"Read the resolution, then run it again with more runs.",
            f"- {_SD.UNRESOLVED}: the resolution is no better than the band itself, so this row could "
            f"not have caught a regression worth failing on.",
            "",
            "#### Notes",
            "",
            "- Each run is a fresh JVM that solves the quickstart's demo dataset for the time limit.",
            "- A quickstart name links to the one GitHub Actions artifact holding the JFR recordings "
            "of both sides.",
            f"- Measured on `{_SD.RUNNER_LABEL}`."]


def render_report(rows: list, baseline_ref: str, branch_ref: str, owner: str) -> tuple[str, int]:
    counts = {}
    for row in rows:
        counts[row[4]] = counts.get(row[4], 0) + 1
    header = " · ".join(f"{v.emoji} {v.label} {counts[v]}" for v in _SD._ALL_VERDICTS if v in counts)

    lines = [f"### {header}", "",
             f"_Old_: [TimefoldAI's {baseline_ref}]({_SD.ref_url('TimefoldAI', baseline_ref)})  ",
             f"_New_: [{owner}'s {branch_ref}]({_SD.ref_url(owner, branch_ref)})", ""]
    lines.extend(render_table(rows))
    lines.extend(render_legend())

    failing = any(row[4] in _SD._FAILING_VERDICTS for row in rows)
    return "\n".join(lines), 1 if failing else 0


def build_report(data_dir: str, expect: list, baseline_ref: str, branch_ref: str, owner: str) -> tuple[str, int]:
    baseline_data, sut_data, asset_urls = load_all(data_dir)
    rows = build_rows(expect, baseline_data, sut_data, asset_urls)
    return render_report(rows, baseline_ref, branch_ref, owner)


def _selftest() -> None:
    import tempfile

    def side(score, lo, hi):
        return {"score": score, "conf_lo": lo, "conf_hi": hi, "forks": []}

    fast = side(100.0, 99.0, 101.0)
    slow = side(80.0, 79.0, 81.0)
    same = side(100.5, 99.5, 101.5)

    assert math.isclose(resolution(fast, fast), math.sqrt(2)), resolution(fast, fast)
    assert evaluate_row(fast, slow)[1] == _SD.REGRESSION
    assert evaluate_row(slow, fast)[1] == _SD.IMPROVEMENT
    assert evaluate_row(fast, same)[1] == _SD.TOLERANCE
    delta, verdict, resolution_pct = evaluate_row(None, fast)
    assert verdict == _SD.MISSING and delta is None and math.isnan(resolution_pct)

    header = "quickstart,runs,mean,min,max,std_dev,ci_lower,ci_upper\n"
    with tempfile.TemporaryDirectory() as data_dir:
        files = {
            "data-a/baseline.csv": header + "a,20,100,98,102,1,99,101\n",
            "data-a/sut.csv": header + "a,20,80,78,82,1,79,81\n",
            "data-a/assets-url.txt": "https://example.com/a\n",
            "data-b/baseline.csv": header + "b,20,100,98,102,1,99,101\n",
            "data-b/sut.csv": header + "b,0,,,,,,\n",
            # One run per side: means within tolerance, but no confidence bounds.
            "data-d/baseline.csv": header + "d,1,100,100,100,,,\n",
            "data-d/sut.csv": header + "d,1,101,101,101,,,\n",
            "data-e/baseline.csv": header + "e,20,0,0,0,0,0,0\n",
            "data-e/sut.csv": header + "e,20,0,0,0,0,0,0\n",
        }
        for name, content in files.items():
            os.makedirs(os.path.join(data_dir, os.path.dirname(name)), exist_ok=True)
            with open(os.path.join(data_dir, name), "w") as f:
                f.write(content)
        baseline, sut, _ = load_all(data_dir)
        assert set(baseline) == {"a", "b"} and set(sut) == {"a"}, (baseline, sut)
        report, exit_code = build_report(data_dir, ["a", "b", "c", "d", "e"], "v2.8.0", "main", "TimefoldAI")
        assert exit_code == 1
        assert "[A](https://example.com/a)" in report, report
        assert "missing data 4" in report, report  # b's SUT failed, c has no data, d has no bounds, e is zero
        assert "100 → 80 (-20.0 %)" in report, report

    print("summarize-quickstarts.py: selftest OK")


def main() -> None:
    if "--selftest" in sys.argv:
        _selftest()
        return

    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("data_dir", help="Directory holding one subdirectory per data-<quickstart> artifact")
    parser.add_argument("--expect", required=True, help="JSON array of expected quickstarts, e.g. [\"vehicle-routing\"]")
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
