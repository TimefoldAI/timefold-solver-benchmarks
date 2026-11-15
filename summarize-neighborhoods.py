#!/usr/bin/env python3
"""Join the move selectors vs. Neighborhoods subjobs into one report table.

Each CI subjob (one per quickstart) uploads a "data-<row>" artifact holding "selectors.csv"
and "neighborhoods.csv" - the output of benchmark-quickstarts.py for that one quickstart, both
variants run in turn with the same seed for the same run index - and "assets-url.txt".

The verdict is the best score. Run N of each side uses seed N, so the runs pair up; each pair is a
win, a loss or a tie for Neighborhoods. A quickstart counts as better or worse only if the gap
between wins and losses is too big to come from chance; otherwise there is no clear difference.
The move evaluation speed is context that can explain a verdict; it is not judged.

Its exit code is the CI verdict: 0 only if every expected row is present and none is worse.

Usage:
  python3 summarize-neighborhoods.py DATA_DIR --expect JSON_ARRAY --branch REF --owner OWNER
  python3 summarize-neighborhoods.py --selftest
"""
import argparse
import collections
import csv
import importlib.util
import json
import math
import os
import re
import statistics
import sys


def _load_sibling_module(file_name: str, module_name: str):
    """The file names have hyphens, so they cannot be imported by name."""
    path = os.path.join(os.path.dirname(os.path.abspath(__file__)), file_name)
    spec = importlib.util.spec_from_file_location(module_name, path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


_SD = _load_sibling_module("summarize-scoredirector.py", "summarize_scoredirector")

SIDES = ("selectors", "neighborhoods")
# Chance of a won/lost gap this big or bigger if both sides were equally good, below which it counts.
CHANCE_LIMIT = 0.05

Verdict = collections.namedtuple("Verdict", ("emoji", "label"))
WORSE = Verdict("‼️", "worse score")
BETTER = Verdict("🚀", "better score")
UNCLEAR = Verdict("✅", "no clear difference")
MISSING = _SD.MISSING
_ALL_VERDICTS = (WORSE, MISSING, BETTER, UNCLEAR)
_FAILING_VERDICTS = {WORSE, MISSING}

_LEVEL_RE = re.compile(r"(-?\d+(?:\.\d+)?)[A-Za-z]+")


def parse_score(raw: str) -> tuple:
    """"0hard/-12soft" -> (0.0, -12.0); levels compare left to right, higher is better."""
    levels = tuple(float(m) for m in _LEVEL_RE.findall(raw))
    if not levels:
        raise ValueError(f"Cannot parse the score ({raw}).")
    return levels


def load_side(path: str) -> dict:
    """Quickstart row -> mean speed and best scores ordered by run index; rows without runs are left out."""
    result = {}
    with open(path, newline="") as f:
        for row in csv.DictReader(f):
            scores = row.get("best_scores") or ""
            if row["mean"] and scores:
                result[row["quickstart"]] = {"speed": float(row["mean"]), "scores": scores.split(";")}
    return result


def load_all(data_dir: str) -> tuple[dict, dict, dict]:
    """Per side: row -> {"speed", "scores"}; and row -> assets artifact URL."""
    data = {side: {} for side in SIDES}
    asset_urls = {}
    for name in sorted(os.listdir(data_dir)):
        row_dir = os.path.join(data_dir, name)
        if not os.path.isdir(row_dir):
            continue
        in_dir = set()
        for side in SIDES:
            path = os.path.join(row_dir, f"{side}.csv")
            if os.path.exists(path):
                side_data = load_side(path)
                data[side].update(side_data)
                in_dir.update(side_data)
        url_path = os.path.join(row_dir, "assets-url.txt")
        url = open(url_path).read().strip() if os.path.exists(url_path) else ""
        if url:
            asset_urls.update((row, url) for row in in_dir)
    return data[SIDES[0]], data[SIDES[1]], asset_urls


def compare_scores(selectors: list, neighborhoods: list) -> tuple[int, int, int]:
    """Wins, losses and ties of Neighborhoods; run i of one side pairs with run i of the other (same seed)."""
    wins = losses = ties = 0
    for s, n in zip(selectors, neighborhoods):
        s_levels, n_levels = parse_score(s), parse_score(n)
        if len(s_levels) != len(n_levels):
            raise ValueError(f"The scores ({s}) and ({n}) have different levels.")
        if n_levels > s_levels:
            wins += 1
        elif n_levels < s_levels:
            losses += 1
        else:
            ties += 1
    return wins, losses, ties


def chance_of_gap(wins: int, losses: int) -> float:
    """Chance of a won/lost gap at least this big if each decided pair were a coin toss (two-sided sign test)."""
    decided = wins + losses
    if decided == 0:
        return 1.0
    tail = sum(math.comb(decided, k) for k in range(min(wins, losses) + 1)) / 2 ** decided
    return min(1.0, 2 * tail)


def score_verdict(wins: int, losses: int) -> Verdict:
    if wins != losses and chance_of_gap(wins, losses) < CHANCE_LIMIT:
        return BETTER if wins > losses else WORSE
    return UNCLEAR


def median_score(scores: list) -> str:
    """The lower median, so it is a score that a run really reached."""
    ordered = sorted(scores, key=parse_score)
    return ordered[(len(ordered) - 1) // 2]


def build_rows(expect: list, selectors_data: dict, neighborhoods_data: dict, asset_urls: dict) -> list:
    rows = []
    for name in sorted(expect):
        old, new = selectors_data.get(name), neighborhoods_data.get(name)
        if old is None or new is None:
            rows.append((name, old, new, None, MISSING, asset_urls.get(name)))
            continue
        wlt = compare_scores(old["scores"], new["scores"])
        rows.append((name, old, new, wlt, score_verdict(wlt[0], wlt[1]), asset_urls.get(name)))
    return rows


def format_speed(old: dict, new: dict) -> str:
    delta_pct = (new["speed"] / old["speed"] - 1) * 100
    return f"{_SD.format_count(old['speed'])} → {_SD.format_count(new['speed'])} ({delta_pct:+.1f} %)"


def format_chance(wins: int, losses: int) -> str:
    if wins + losses == 0:
        return _SD.ABSENT
    chance = chance_of_gap(wins, losses)
    return "< 1 %" if chance < 0.01 else f"{chance * 100:.0f} %"


def render_table(rows: list) -> list:
    lines = ["| Quickstart | Best score | Neighborhoods won / lost / tied | Chance if equal | Median best score "
             "| Move evaluation speed |",
             "|---|:---:|---:|---:|---:|---:|"]
    for name, old, new, wlt, verdict, url in rows:
        if wlt is None:
            lines.append(f"| {_SD.format_example(name, url)} | {verdict.emoji} | | | | |")
            continue
        lines.append("| {} | {} | {} / {} / {} | {} | {} → {} | {} |".format(
            _SD.format_example(name, url), verdict.emoji, *wlt, format_chance(wlt[0], wlt[1]),
            median_score(old["scores"]), median_score(new["scores"]), format_speed(old, new)))
    return lines


def render_legend() -> list:
    return ["",
            " · ".join(f"{v.emoji} {v.label}" for v in _ALL_VERDICTS),
            "",
            "#### How to read the table",
            "",
            "- Old is move selectors, new is Neighborhoods. Both list the same moves, in the same order, "
            "with equal weights. Move selectors also draw moves that change nothing, such as a change to the "
            "current value, and skip them; Neighborhoods do not make them. So the mix of evaluated moves "
            "can differ, which is part of what this report measures.",
            "- Best score is the verdict. Run N of each side uses random seed N; each pair is a win, a loss "
            "or a tie for Neighborhoods, by the higher best score.",
            f"- {BETTER.emoji} or {WORSE.emoji} only when the gap between wins and losses is too big to come "
            f"from chance: if both sides were equally good, a gap this big would happen less than "
            f"{CHANCE_LIMIT:.0%} of the time. Ties do not count. Otherwise {UNCLEAR.emoji}.",
            "- Chance if equal: how often a gap between wins and losses at least this big would happen "
            "if both sides were equally good, like tossing a fair coin once per pair that is not a tie. "
            "The lower it is, the less the result can be luck.",
            f"- {WORSE.emoji} and {MISSING.emoji} fail the build.",
            "- Median best score: selectors → Neighborhoods.",
            "- Move evaluation speed: the mean in moves/s, selectors → Neighborhoods, "
            "with (Neighborhoods / selectors - 1) × 100 in brackets. It is not judged; "
            "it can explain a score verdict, as a faster side tries more moves in the same time.",
            "",
            "#### Notes",
            "",
            "- Each run is a fresh JVM that solves the quickstart's largest demo dataset for the time limit; "
            "a name ending in -LARGE is the LARGE dataset, any other name is the default one.",
            "- vehicle-routing is not included: Neighborhoods do not support nearby selection yet.",
            "- A quickstart name links to the GitHub Actions artifact holding the JFR recording of seed 1 "
            "of each side, made with profiling.jfc.",
            f"- Measured on `{_SD.RUNNER_LABEL}`."]


def render_report(rows: list, branch_ref: str, owner: str) -> tuple[str, int]:
    counts = collections.Counter(row[4] for row in rows)
    header = " · ".join(f"{v.emoji} {v.label} {counts[v]}" for v in _ALL_VERDICTS if v in counts)
    lines = [f"### {header}", "",
             f"_Branch_: [{owner}'s {branch_ref}]({_SD.ref_url(owner, branch_ref)})", ""]
    lines.extend(render_table(rows))
    lines.extend(render_legend())
    failing = any(row[4] in _FAILING_VERDICTS for row in rows)
    return "\n".join(lines), 1 if failing else 0


def build_report(data_dir: str, expect: list, branch_ref: str, owner: str) -> tuple[str, int]:
    selectors_data, neighborhoods_data, asset_urls = load_all(data_dir)
    rows = build_rows(expect, selectors_data, neighborhoods_data, asset_urls)
    return render_report(rows, branch_ref, owner)


def _selftest() -> None:
    import tempfile

    assert parse_score("0hard/-12soft") == (0.0, -12.0)
    assert parse_score("-1hard/0medium/-3.5soft") == (-1.0, 0.0, -3.5)
    assert parse_score("0hard/3358.68344soft") == (0.0, 3358.68344)
    assert compare_scores(["0hard/-5soft", "-1hard/0soft", "0hard/0soft"],
                          ["0hard/-4soft", "0hard/-9soft", "0hard/0soft"]) == (2, 0, 1)
    assert compare_scores(["0hard/-5soft"], ["-1hard/0soft"]) == (0, 1, 0)
    assert median_score(["0hard/-1soft", "0hard/-3soft", "0hard/-2soft", "0hard/-4soft"]) == "0hard/-3soft"
    assert math.isclose(chance_of_gap(15, 5), 0.0414, abs_tol=1e-4), chance_of_gap(15, 5)
    assert math.isclose(chance_of_gap(12, 8), 0.5034, abs_tol=1e-4), chance_of_gap(12, 8)
    assert chance_of_gap(0, 0) == 1.0 and chance_of_gap(3, 3) == 1.0
    assert score_verdict(15, 5) == BETTER and score_verdict(5, 15) == WORSE
    assert score_verdict(12, 8) == UNCLEAR and score_verdict(0, 0) == UNCLEAR
    assert format_chance(12, 8) == "50 %" and format_chance(20, 0) == "< 1 %" and format_chance(0, 0) == "—"
    assert score_verdict(5, 0) == UNCLEAR and score_verdict(6, 0) == BETTER  # 2 × 1/64 < 5 % < 2 × 1/32

    header = "quickstart,runs,mean,min,max,std_dev,ci_lower,ci_upper,best_scores\n"
    better = ";".join(["0hard/-4soft"] * 6)
    worse = ";".join(["0hard/-6soft"] * 6)
    with tempfile.TemporaryDirectory() as data_dir:
        files = {
            "data-a/selectors.csv": header + f"a,6,100,98,102,1,99,101,{worse}\n",
            "data-a/neighborhoods.csv": header + f"a,6,80,78,82,1,79,81,{better}\n",
            "data-a/assets-url.txt": "https://example.com/a\n",
            "data-b/selectors.csv": header + "b,2,100,98,102,1,99,101,0hard/-5soft;0hard/-6soft\n",
            "data-b/neighborhoods.csv": header + "b,0,,,,,,,\n",
            "data-c/selectors.csv": header + f"c,6,100,98,102,1,99,101,{better}\n",
            "data-c/neighborhoods.csv": header + f"c,6,120,118,122,1,119,121,{worse}\n",
            "data-d/selectors.csv": header + "d,1,100,100,100,,,,0hard/-5soft\n",
            "data-d/neighborhoods.csv": header + "d,1,101,101,101,,,,0hard/-5soft\n",
        }
        for name, content in files.items():
            os.makedirs(os.path.join(data_dir, os.path.dirname(name)), exist_ok=True)
            with open(os.path.join(data_dir, name), "w") as f:
                f.write(content)
        report, exit_code = build_report(data_dir, ["a", "d"], "main", "TimefoldAI")
        assert exit_code == 0, report
        assert "| [A](https://example.com/a) | 🚀 | 6 / 0 / 0 | 3 % | 0hard/-6soft → 0hard/-4soft | 100 → 80 (-20.0 %) |" \
            in report, report
        assert "| D | ✅ | 0 / 0 / 1 | — |" in report, report  # one run is enough; speed needs no bounds
        report, exit_code = build_report(data_dir, ["a", "b", "c", "e"], "main", "TimefoldAI")
        assert exit_code == 1, report
        assert "| C | ‼️ | 0 / 6 / 0 | 3 % |" in report, report
        assert "‼️ worse score 1 · ❌ missing data 2 · 🚀 better score 1" in report, report

    print("summarize-neighborhoods.py: selftest OK")


def main() -> None:
    if "--selftest" in sys.argv:
        _selftest()
        return

    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("data_dir", help="Directory holding one subdirectory per data-<row> artifact")
    parser.add_argument("--expect", required=True, help="JSON array of expected rows, e.g. [\"school-timetabling-LARGE\"]")
    parser.add_argument("--branch", required=True)
    parser.add_argument("--owner", required=True)
    parser.add_argument("--selftest", action="store_true", help=argparse.SUPPRESS)
    args = parser.parse_args()

    report, exit_code = build_report(args.data_dir, json.loads(args.expect), args.branch, args.owner)
    print(report)
    sys.exit(exit_code)


if __name__ == "__main__":
    main()
