#!/usr/bin/env python3
"""Benchmark a single Timefold quickstart's score speed.

Usage:
  python3 benchmark.py QUICKSTART DIRECTORY... [--runs N] [--parallel N] [--seed-from-run-index] [--dataset ID] [--time-only]
                       [--jfr-settings FILE]
                       [--time-limit S] [--base-port N] [--output FILE...] [--jfr-dir DIR...]

Measures "move evaluation speed" (moves/sec) and the best score from the solver's "Solving ended:"
log line. Starts a fresh JVM for each run, reports aggregate stats.
With several directories, each run index runs every app in the given order, one after another,
on the same port and with the same seed; each app writes its own output file.

Requires Python 3.10+, Java on PATH.
"""
import argparse
import csv
import math
import queue
import re
import socket
import statistics
import subprocess
import sys
import threading
import time
import urllib.request
from pathlib import Path

SERVER_READY_TIMEOUT = 60   # seconds to wait for "started in" in logs
PORT_FREE_TIMEOUT = 30      # seconds to wait for port to be released

# Quickstart name → REST trigger config of the service module.
# demo: GET endpoint that returns a complete model request
# solve: POST endpoint that accepts the model request and starts solving
QUICKSTARTS: dict[str, dict[str, str]] = {
    "bed-allocation":           {"demo": "/v1/demo-data/BASIC",   "solve": "/v1/schedules"},
    "conference-scheduling":    {"demo": "/v1/demo-data/BASIC",   "solve": "/v1/schedules"},
    "employee-scheduling":      {"demo": "/v1/demo-data/BASIC",   "solve": "/v1/schedules"},
    "facility-location":        {"demo": "/v1/demo-data/BASIC",   "solve": "/v1/plans"},
    "flight-crew-scheduling":   {"demo": "/v1/demo-data/BASIC",   "solve": "/v1/schedules"},
    "food-packaging":           {"demo": "/v1/demo-data/BASIC",   "solve": "/v1/schedules"},
    "maintenance-scheduling":   {"demo": "/v1/demo-data/BASIC",   "solve": "/v1/schedules"},
    "meeting-scheduling":       {"demo": "/v1/demo-data/BASIC",   "solve": "/v1/schedules"},
    "order-picking":            {"demo": "/v1/demo-data/BASIC",   "solve": "/v1/picking-plans"},
    "project-job-scheduling":   {"demo": "/v1/demo-data/BASIC",   "solve": "/v1/schedules"},
    "school-timetabling":       {"demo": "/v1/demo-data/BASIC",   "solve": "/v1/timetables"},
    "sports-league-scheduling": {"demo": "/v1/demo-data/BASIC",   "solve": "/v1/schedules"},
    "task-assigning":           {"demo": "/v1/demo-data/BASIC",   "solve": "/v1/schedules"},
    "tournament-scheduling":    {"demo": "/v1/demo-data/BASIC",   "solve": "/v1/schedules"},
    "vehicle-routing":          {"demo": "/v1/demo-data/FIRENZE", "solve": "/v1/route-plans"},
}

# Matches the solver-level summary line only (not per-phase summaries).
_SPEED_RE = re.compile(
    r"Solving ended: time spent \((\d+)\), best score \(([^)]*)\), move evaluation speed \((\d+)/sec\)")
# The service logs its termination choice; Diminished Returns would end some solves before the time limit.
_DIMINISHED_RETURNS_MARK = "with diminished returns termination"
# A move count limit that no run reaches: the service then ends solving on the time limit alone.
_UNREACHABLE_MOVE_COUNT = 2 ** 63 - 1
_TIME_ONLY_MARK = f"with move count limit ({_UNREACHABLE_MOVE_COUNT}) termination"

_PRINT_LOCK = threading.Lock()


def _log(msg: str) -> None:
    with _PRINT_LOCK:
        print(msg, flush=True)


def _is_port_free(port: int) -> bool:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.settimeout(0.5)
        return s.connect_ex(("127.0.0.1", port)) != 0


def _wait_port_free(port: int, timeout: int = PORT_FREE_TIMEOUT) -> bool:
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        if _is_port_free(port):
            return True
        time.sleep(0.5)
    return False


def _kill(proc: subprocess.Popen) -> None:
    if proc.poll() is not None:
        return
    proc.terminate()
    try:
        proc.wait(timeout=10)
    except subprocess.TimeoutExpired:
        proc.kill()
        proc.wait()


def _run_once(
    name: str,
    config: dict,
    directory: Path,
    run_idx: int,
    total_runs: int,
    port: int,
    time_limit: int,
    jfr_file: Path | None,
    seed: int | None,
    time_only: bool,
    jfr_settings: str,
) -> tuple[int, str] | None:
    """Start server, trigger solving, capture result, kill server. Returns (moves/sec, best score) or None."""
    base_url = f"http://127.0.0.1:{port}"
    run_timeout = time_limit + 120  # generous margin beyond solver time
    jar = directory / "quarkus-run.jar"

    if not _wait_port_free(port):
        _log(f"[{name}] run {run_idx}/{total_runs}: ERR (port {port} still busy)")
        return None

    if time_only:
        # The service has no termination on time alone; without an unimproved limit, an unreachable move limit gives it.
        termination_args = ["-Dtimefold.model.termination.unimproved-spent-limit=",
                            f"-Dtimefold.model.termination.move-count-limit={_UNREACHABLE_MOVE_COUNT}"]
    else:
        # Replaces the service default (Diminished Returns), which ends solves early; it cannot fire before the spent limit.
        termination_args = [f"-Dtimefold.model.termination.unimproved-spent-limit=PT{time_limit}S"]
    jfr_args = [f"-XX:StartFlightRecording=settings={jfr_settings},filename={jfr_file}"] if jfr_file else []
    seed_args = [f"-Dquarkus.timefold.solver.random-seed={seed}"] if seed is not None else []
    proc = subprocess.Popen(
        [
            "java",
            *jfr_args,
            *seed_args,
            f"-Dquarkus.http.port={port}",
            f"-Dtimefold.model.termination.spent-limit=PT{time_limit}S",
            *termination_args,
            "-Dquarkus.timefold.solver.move-thread-count=NONE",
            "-Dquarkus.log.category.ai.timefold.solver.level=INFO",
            "-Dquarkus.console.color=false",
            "-jar", str(jar),
        ],
        cwd=directory,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        bufsize=1,
    )

    speed_event = threading.Event()
    ready_event = threading.Event()
    result_box: list[tuple[int, int, str] | None] = [None]
    diminished_returns = threading.Event()
    time_only_seen = threading.Event()

    def _reader() -> None:
        # Drain continuously; prevents stdout buffer from filling (which would block proc.wait()).
        for line in proc.stdout:
            if not ready_event.is_set() and "started in" in line:
                ready_event.set()
            if _DIMINISHED_RETURNS_MARK in line:
                diminished_returns.set()
            if _TIME_ONLY_MARK in line:
                time_only_seen.set()
            if not speed_event.is_set():
                m = _SPEED_RE.search(line)
                if m:
                    result_box[0] = (int(m.group(1)), int(m.group(3)), m.group(2))
                    speed_event.set()
        # Loop exits naturally when the pipe closes after the server is killed.

    reader_thread = threading.Thread(target=_reader, daemon=True)
    reader_thread.start()

    try:
        # Wait for Quarkus "started in" log line.
        if not ready_event.wait(timeout=SERVER_READY_TIMEOUT):
            if proc.poll() is not None:
                _log(f"[{name}] run {run_idx}/{total_runs}: ERR (server exited during startup)")
            else:
                _log(f"[{name}] run {run_idx}/{total_runs}: ERR (server not ready in {SERVER_READY_TIMEOUT}s)")
            return None

        # Fetch the demo model request and post it as-is; posting starts solving.
        with urllib.request.urlopen(f"{base_url}{config['demo']}", timeout=30) as resp:
            body = resp.read()
        req = urllib.request.Request(
            f"{base_url}{config['solve']}",
            data=body,
            headers={"Content-Type": "application/json"},
            method="POST",
        )
        with urllib.request.urlopen(req, timeout=30):
            pass  # returns the dataset metadata; we don't need it

        # Wait for "Solving ended:" in server logs.
        if speed_event.wait(timeout=run_timeout):
            spent_millis, speed, score = result_box[0]
            # Every run must solve for the full time limit, so that both sides of a comparison get the same time.
            if diminished_returns.is_set():
                _log(f"[{name}] run {run_idx}/{total_runs}: ERR (Diminished Returns termination is active)")
                return None
            if time_only and not time_only_seen.is_set():
                _log(f"[{name}] run {run_idx}/{total_runs}: ERR (the service did not log the time-only termination)")
                return None
            if spent_millis < time_limit * 1000:
                _log(f"[{name}] run {run_idx}/{total_runs}: ERR (solving ended after {spent_millis} ms, "
                     f"before the time limit of {time_limit} s)")
                return None
            _kill(proc)  # The JVM writes the JFR recording when it exits.
            if jfr_file and not (jfr_file.is_file() and jfr_file.stat().st_size > 0):
                _log(f"[{name}] run {run_idx}/{total_runs}: ERR (no JFR recording in {jfr_file})")
                return None
            _log(f"[{name}] run {run_idx}/{total_runs}: {speed:,} moves/s, best score {score}")
            return speed, score

        _log(f"[{name}] run {run_idx}/{total_runs}: ERR (no result after {run_timeout}s)")
        return None

    except Exception as e:
        _log(f"[{name}] run {run_idx}/{total_runs}: ERR ({e})")
        return None
    finally:
        _kill(proc)
        reader_thread.join(timeout=5)


def _ci_bounds(speeds: list[int]) -> tuple[float, float] | None:
    """Return (ci_lower, ci_upper) for a 99.9% two-tailed CI, or None if n < 2."""
    n = len(speeds)
    if n < 2:
        return None
    z = statistics.NormalDist().inv_cdf(0.9995)
    mean = statistics.mean(speeds)
    margin = z * statistics.stdev(speeds) / math.sqrt(n)
    return mean - margin, mean + margin


def _print_table(name: str, speeds: list[int], total_runs: int) -> None:
    print()
    if speeds:
        mean = statistics.mean(speeds)
        std = f"{statistics.stdev(speeds):,.0f}" if len(speeds) >= 2 else "—"
        ci = _ci_bounds(speeds)
        print(f"Quickstart : {name}")
        print(f"Runs       : {len(speeds)}/{total_runs}")
        print(f"Mean       : {mean:,.0f} moves/s")
        print(f"Min        : {min(speeds):,.0f} moves/s")
        print(f"Max        : {max(speeds):,.0f} moves/s")
        print(f"Std Dev    : {std} moves/s")
        if ci:
            print(f"99.9% CI   : {ci[0]:,.0f} – {ci[1]:,.0f} moves/s")
    else:
        print(f"Quickstart : {name}")
        print(f"Runs       : 0/{total_runs} (all failed)")


def _write_csv(name: str, speeds: list[int], scores: list[str], path: str) -> None:
    """The best_scores column lists the best score of every run, ordered by run index."""
    with open(path, "w", newline="") as f:
        w = csv.writer(f)
        w.writerow(["quickstart", "runs", "mean", "min", "max", "std_dev", "ci_lower", "ci_upper", "best_scores"])
        if speeds:
            mean = statistics.mean(speeds)
            std = f"{statistics.stdev(speeds):.0f}" if len(speeds) >= 2 else ""
            ci = _ci_bounds(speeds)
            ci_lower = f"{ci[0]:.0f}" if ci else ""
            ci_upper = f"{ci[1]:.0f}" if ci else ""
            w.writerow([name, len(speeds), f"{mean:.0f}", min(speeds), max(speeds), std, ci_lower, ci_upper,
                        ";".join(scores)])
        else:
            w.writerow([name, 0, "", "", "", "", "", "", ""])
    _log(f"CSV written to {path}")


def main() -> None:
    p = argparse.ArgumentParser(
        description="Benchmark a Timefold quickstart's move evaluation speed",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="Known quickstarts:\n  " + "\n  ".join(QUICKSTARTS),
    )
    p.add_argument("quickstart", metavar="QUICKSTART",
                   help="quickstart name (used to look up REST endpoints)")
    p.add_argument("directories", metavar="DIRECTORY", type=Path, nargs="+",
                   help="path to quarkus-app/ directory (must contain quarkus-run.jar); "
                        "several directories run in turn for each run index")
    p.add_argument("--runs", type=int, default=20, metavar="N",
                   help="number of runs (default: 20)")
    p.add_argument("--parallel", type=int, default=1, metavar="N",
                   help="concurrent server instances, each on its own port (default: 1)")
    p.add_argument("--time-limit", type=int, default=60, metavar="S",
                   help="solver time limit in seconds per run (default: 60)")
    p.add_argument("--base-port", type=int, default=8080, metavar="N",
                   help="first port in the pool; each parallel instance uses base-port+i (default: 8080)")
    p.add_argument("--dataset", metavar="ID",
                   help="demo dataset ID to solve instead of the quickstart's default; "
                        "the result row is then named QUICKSTART-ID")
    p.add_argument("--jfr-settings", default="default", metavar="FILE",
                   help="JFR settings for --jfr-dir: a .jfc file, or the name of a JDK one (default: default)")
    p.add_argument("--time-only", action="store_true",
                   help="end each solve on the time limit alone; needs a service that knows the move count limit")
    p.add_argument("--seed-from-run-index", action="store_true",
                   help="solve run N with random seed N, so the same run index of each directory shares a seed")
    p.add_argument("--output", nargs="+", default=["benchmark-results.csv"], metavar="FILE",
                   help="CSV output path, one per directory (default: benchmark-results.csv)")
    p.add_argument("--jfr-dir", nargs="+", type=Path, metavar="DIR",
                   help="record each run with JFR (see --jfr-settings) into DIR/run-NN.jfr, one DIR per directory")
    args = p.parse_args()

    name = args.quickstart
    if name not in QUICKSTARTS:
        p.error(f"Unknown quickstart: {name!r}\nKnown: {', '.join(QUICKSTARTS)}")
    config = QUICKSTARTS[name]
    if args.dataset:
        config = {**config, "demo": f"{config['demo'].rsplit('/', 1)[0]}/{args.dataset}"}
        name = f"{name}-{args.dataset}"
    if len(args.output) != len(args.directories):
        p.error(f"Give one --output per directory ({len(args.directories)}), not {len(args.output)}.")
    if args.jfr_dir and len(args.jfr_dir) != len(args.directories):
        p.error(f"Give one --jfr-dir per directory ({len(args.directories)}), not {len(args.jfr_dir)}.")

    directories = [d.resolve() for d in args.directories]
    for directory in directories:
        if not directory.is_dir():
            p.error(f"Directory not found: {directory}")
        jar = directory / "quarkus-run.jar"
        if not jar.is_file():
            p.error(f"JAR not found: {jar}\nExpected quarkus-app/ directory with quarkus-run.jar inside.")
    labels = [name] if len(directories) == 1 else [f"{name}/{d.name}" for d in directories]

    base_port = args.base_port
    _log(
        f"=== Benchmarking {name}: {args.runs} run(s)"
        f", parallel={args.parallel}, time-limit={args.time_limit}s ==="
    )
    for directory in directories:
        _log(f"    Directory : {directory}")
    _log(f"    Dataset   : {config['demo']}")
    _log(f"    Ports     : {base_port}–{base_port + args.parallel - 1}\n")

    # A .jfc path must stay valid in the JVM, whose working directory is the app directory.
    jfr_settings = str(Path(args.jfr_settings).resolve()) if args.jfr_settings.endswith(".jfc") else args.jfr_settings
    if args.jfr_settings.endswith(".jfc") and not Path(jfr_settings).is_file():
        p.error(f"JFR settings file not found: {jfr_settings}")
    jfr_dirs = [d.resolve() for d in args.jfr_dir] if args.jfr_dir else [None] * len(directories)
    for jfr_dir in jfr_dirs:
        if jfr_dir:
            jfr_dir.mkdir(parents=True, exist_ok=True)

    # Port pool: one slot per parallel instance, each on a distinct port.
    port_q: queue.Queue[int] = queue.Queue()
    for i in range(args.parallel):
        port_q.put(base_port + i)

    # Shared run counter: workers claim the next run index atomically.
    # abort event: set by any worker on failure; stops all remaining work.
    # Results are keyed by run index, so the CSV keeps the run order even with parallel workers.
    results: list[dict[int, tuple[int, str]]] = [{} for _ in directories]
    results_lock = threading.Lock()
    counter_lock = threading.Lock()
    run_counter = [1]
    abort = threading.Event()

    def _worker() -> None:
        port = port_q.get()
        try:
            while not abort.is_set():
                with counter_lock:
                    run_idx = run_counter[0]
                    if run_idx > args.runs:
                        break
                    run_counter[0] += 1
                seed = run_idx if args.seed_from_run_index else None
                for i, directory in enumerate(directories):
                    jfr_file = jfr_dirs[i] / f"run-{run_idx:02d}.jfr" if jfr_dirs[i] else None
                    r = _run_once(labels[i], config, directory, run_idx, args.runs, port, args.time_limit,
                                  jfr_file, seed, args.time_only, jfr_settings)
                    if r is None:
                        abort.set()
                        return
                    with results_lock:
                        results[i][run_idx] = r
        finally:
            port_q.put(port)

    threads = [
        threading.Thread(target=_worker, daemon=True, name=f"worker-{i}")
        for i in range(args.parallel)
    ]
    for t in threads:
        t.start()
    for t in threads:
        t.join()

    failed = False
    for i, result in enumerate(results):
        ordered = [result[k] for k in sorted(result)]
        speeds = [speed for speed, _ in ordered]
        _print_table(labels[i], speeds, args.runs)
        _write_csv(name, speeds, [score for _, score in ordered], args.output[i])
        failed |= len(speeds) < args.runs
    if failed:
        sys.exit("Not all runs succeeded.")


if __name__ == "__main__":
    main()
