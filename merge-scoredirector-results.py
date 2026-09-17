#!/usr/bin/env python3
"""Merge the single-fork JMH result files of one side into the one file the report expects.

CI runs the baseline and the SUT one fork at a time, alternating, so that anything the machine
does lands on both sides equally instead of on whichever block it happens to cover. That leaves one
results.json for each fork. This script joins them back into a single entry for each benchmark, identical in
shape to what a single multi-fork JMH run would have written, so summarize-scoredirector.py needs
no knowledge of how the measurements were collected.

A one-fork JMH file reports "scoreError": NaN, which the summarizer reads as "cannot say which side
is faster". The merge therefore has to happen before the data artifact is built, not in the report.

Usage:
  python3 merge-scoredirector-results.py FORK_RESULTS... -o MERGED [--expect-jdk MAJOR]
  python3 merge-scoredirector-results.py --selftest

Pass the fork files in the order they ran; the merged rawData keeps that order, which is what makes
fork N of the merged JFR line up with fork N here.
"""
import argparse
import json
import math
import statistics
import sys

# JMH reports the half-width of a 99.9 % confidence interval, so each tail holds 0.05 %.
CONFIDENCE_TAIL = 0.0005

# A fork keeps whatever shape the JIT gave it for its whole life, so the iterations inside one fork
# are repeated looks at one sample, not independent ones. Every statistic below is therefore
# computed over fork means. With one measurement iteration for each fork this is exactly what JMH
# would have computed itself; it stays correct if the iteration count is ever raised again.
PERCENTILES = ("0.0", "50.0", "90.0", "95.0", "99.0", "99.9", "99.99", "99.999", "99.9999", "100.0")

# Fields describing the JVM that ran the benchmark. They must agree across every fork of one side -
# a fork that quietly ran on another JDK makes the comparison meaningless, so it is an error here
# rather than something to notice later in a report.
JVM_FIELDS = ("jvm", "jdkVersion", "vmName", "vmVersion")


def _incomplete_beta(a: float, b: float, x: float) -> float:
    """Regularised incomplete beta, by the usual continued fraction. Avoids a scipy dependency."""
    if x <= 0:
        return 0.0
    if x >= 1:
        return 1.0

    def fraction(a: float, b: float, x: float) -> float:
        tiny = 1e-30
        c, d = 1.0, 1 - (a + b) * x / (a + 1)
        d = 1 / (d if abs(d) > tiny else tiny)
        h = d
        for m in range(1, 300):
            m2 = 2 * m
            for numerator in (m * (b - m) * x / ((a - 1 + m2) * (a + m2)),
                              -(a + m) * (a + b + m) * x / ((a + m2) * (a + 1 + m2))):
                d = 1 + numerator * d
                d = 1 / (d if abs(d) > tiny else tiny)
                c = 1 + numerator / (c if abs(c) > tiny else tiny)
                c = c if abs(c) > tiny else tiny
                delta = d * c
                h *= delta
            if abs(delta - 1) < 3e-16:
                break
        return h

    log_beta = math.lgamma(a) + math.lgamma(b) - math.lgamma(a + b)
    front = math.exp(a * math.log(x) + b * math.log(1 - x) - log_beta)
    if x < (a + 1) / (a + b + 2):
        return front / a * fraction(a, b, x)
    return 1 - math.exp(b * math.log(1 - x) + a * math.log(x) - log_beta) / b * fraction(b, a, 1 - x)


def t_upper_tail(t: float, degrees_of_freedom: int) -> float:
    return _incomplete_beta(degrees_of_freedom / 2, 0.5,
                            degrees_of_freedom / (degrees_of_freedom + t * t)) / 2


def t_quantile(tail: float, degrees_of_freedom: int) -> float:
    """The t value whose upper tail is `tail`, found by bisection. Monotonic, so this is safe."""
    low, high = 0.0, 1.0
    while t_upper_tail(high, degrees_of_freedom) > tail:
        high *= 2
        if high > 1e6:
            raise ValueError("The t quantile did not bracket; degrees of freedom (%d) must be positive."
                             % degrees_of_freedom)
    for _ in range(200):
        middle = (low + high) / 2
        if t_upper_tail(middle, degrees_of_freedom) > tail:
            low = middle
        else:
            high = middle
    return (low + high) / 2


def percentile(sorted_values: list, rank: float) -> float:
    """Linear interpolation between the neighbouring values, as JMH's own percentiles do."""
    if not sorted_values:
        return math.nan
    position = (len(sorted_values) - 1) * rank / 100
    below = math.floor(position)
    above = min(below + 1, len(sorted_values) - 1)
    return sorted_values[below] + (sorted_values[above] - sorted_values[below]) * (position - below)


def entry_key(entry: dict) -> str:
    return json.dumps([entry["benchmark"], entry["params"]], sort_keys=True)


def merge_entries(entries: list) -> dict:
    """Builds one JMH entry out of the same benchmark run once for each fork."""
    first = entries[0]
    for other in entries[1:]:
        for field in JVM_FIELDS:
            if other[field] != first[field]:
                raise ValueError("The %s (%s) of one fork does not match the other forks (%s). "
                                 "Maybe the two sides did not run on the same JDK."
                                 % (field, other[field], first[field]))

    raw_data = [fork for entry in entries for fork in entry["primaryMetric"]["rawData"]]
    iteration_counts = {len(fork) for fork in raw_data}
    if len(iteration_counts) != 1:
        raise ValueError("The forks (%s) do not all have the same iteration count."
                         % sorted(iteration_counts))
    fork_means = [statistics.fmean(fork) for fork in raw_data]
    fork_count = len(fork_means)
    if fork_count < 2:
        raise ValueError("The forkCount (%d) must be at least 2 for an error to be computable."
                         % fork_count)

    score = statistics.fmean(fork_means)
    error = t_quantile(CONFIDENCE_TAIL, fork_count - 1) \
        * statistics.stdev(fork_means) / math.sqrt(fork_count)
    ordered = sorted(value for fork in raw_data for value in fork)

    merged = dict(first)
    merged["forks"] = fork_count
    merged["measurementIterations"] = iteration_counts.pop()
    merged["primaryMetric"] = dict(first["primaryMetric"])
    merged["primaryMetric"].update({
        "score": score,
        "scoreError": error,
        "scoreConfidence": [score - error, score + error],
        "scorePercentiles": {rank: percentile(ordered, float(rank)) for rank in PERCENTILES},
        "rawData": raw_data,
    })
    return merged


def merge_files(paths: list, expected_jdk: "str | None" = None) -> list:
    grouped = {}
    for path in paths:
        with open(path) as file:
            for entry in json.load(file):
                grouped.setdefault(entry_key(entry), []).append(entry)
    if not grouped:
        raise ValueError("The fileCount (%d) held no benchmark results." % len(paths))

    merged = [merge_entries(entries) for entries in grouped.values()]
    if expected_jdk is not None:
        for entry in merged:
            actual = str(entry["jdkVersion"])
            if actual != expected_jdk and not actual.startswith(expected_jdk + "."):
                raise ValueError("The jdkVersion (%s) of benchmark (%s) is not the requested "
                                 "major version (%s)." % (actual, entry["benchmark"], expected_jdk))
    return merged


def _selftest() -> None:
    # JMH itself used 3.392 for 100 iterations; recovering that number validates the quantile.
    assert abs(t_quantile(CONFIDENCE_TAIL, 99) - 3.392) < 0.001, t_quantile(CONFIDENCE_TAIL, 99)
    for degrees_of_freedom, expected in ((1, 636.619), (10, 4.587), (19, 3.883), (25, 3.725)):
        actual = t_quantile(CONFIDENCE_TAIL, degrees_of_freedom)
        assert abs(actual - expected) < max(0.001, expected * 1e-5), (degrees_of_freedom, actual)

    assert percentile([1.0, 2.0, 3.0], 0.0) == 1.0
    assert percentile([1.0, 2.0, 3.0], 50.0) == 2.0
    assert percentile([1.0, 2.0, 3.0], 100.0) == 3.0

    def fork(*values, jvm="/jdk25/bin/java", version="25.0.4.1"):
        return {"benchmark": "Bench.run", "params": {"csExample": "CLOUD_BALANCING"},
                "jvm": jvm, "jdkVersion": version, "vmName": "OpenJDK 64-Bit Server VM",
                "vmVersion": "25+1", "forks": 1, "measurementIterations": len(values),
                "primaryMetric": {"score": statistics.fmean(values), "scoreError": float("nan"),
                                  "scoreConfidence": [float("nan"), float("nan")],
                                  "scorePercentiles": {}, "scoreUnit": "ops/s",
                                  "rawData": [list(values)]}}

    merged = merge_entries([fork(100.0), fork(102.0), fork(98.0), fork(104.0)])
    assert merged["forks"] == 4 and merged["measurementIterations"] == 1
    assert merged["primaryMetric"]["score"] == 101.0
    expected_error = t_quantile(CONFIDENCE_TAIL, 3) * statistics.stdev([100.0, 102.0, 98.0, 104.0]) / 2
    assert abs(merged["primaryMetric"]["scoreError"] - expected_error) < 1e-9
    low, high = merged["primaryMetric"]["scoreConfidence"]
    assert abs(high - low - 2 * expected_error) < 1e-9
    assert merged["primaryMetric"]["rawData"] == [[100.0], [102.0], [98.0], [104.0]], "fork order is kept"
    assert merged["primaryMetric"]["scorePercentiles"]["0.0"] == 98.0
    assert merged["primaryMetric"]["scoreUnit"] == "ops/s", "unrelated fields survive"

    # Iterations inside a fork are not independent, so they must not inflate the sample count.
    spread = merge_entries([fork(100.0, 100.0, 100.0), fork(104.0, 104.0, 104.0)])
    assert spread["measurementIterations"] == 3 and spread["forks"] == 2
    assert spread["primaryMetric"]["score"] == 102.0
    two_fork_error = t_quantile(CONFIDENCE_TAIL, 1) * statistics.stdev([100.0, 104.0]) / math.sqrt(2)
    assert abs(spread["primaryMetric"]["scoreError"] - two_fork_error) < 1e-9

    for broken, reason in (
            ([fork(100.0), fork(102.0, jvm="/jdk21/bin/java")], "a different JVM"),
            ([fork(100.0), fork(102.0, version="21.0.1")], "a different JDK version"),
            ([fork(100.0), fork(102.0, 103.0)], "a different iteration count"),
            ([fork(100.0)], "a single fork")):
        try:
            merge_entries(broken)
            raise AssertionError("merge_entries accepted " + reason)
        except ValueError:
            pass

    print("merge-scoredirector-results.py: selftest OK")


def main() -> None:
    if "--selftest" in sys.argv:
        _selftest()
        return

    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("results", nargs="+", help="Single-fork results.json files, in the order they ran")
    parser.add_argument("-o", "--output", required=True, help="Where to write the merged results.json")
    parser.add_argument("--expect-jdk", help="Major JDK version every fork must report, e.g. 25")
    parser.add_argument("--selftest", action="store_true", help=argparse.SUPPRESS)
    args = parser.parse_args()

    try:
        merged = merge_files(args.results, args.expect_jdk)
    except ValueError as error:
        # A traceback tells a CI log reader nothing they can act on.
        print("::error::%s" % error, file=sys.stderr)
        sys.exit(1)
    with open(args.output, "w") as file:
        json.dump(merged, file, indent=4)
    for entry in merged:
        metric = entry["primaryMetric"]
        print("%s: %d forks, %.3f %s ± %.2f %%"
              % (entry["benchmark"], entry["forks"], metric["score"], metric["scoreUnit"],
                 abs(metric["scoreError"] / metric["score"]) * 100))


if __name__ == "__main__":
    main()
