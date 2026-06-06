#!/usr/bin/env python3
"""Benchmark a single Timefold quickstart's score speed.

Usage:
  python3 benchmark.py QUICKSTART DIRECTORY [--runs N] [--parallel N]
                       [--time-limit S] [--base-port N] [--output FILE]

Measures "move evaluation speed" (moves/sec) from the solver's "Solving ended:"
log line. Starts a fresh JVM for each run, reports aggregate stats.

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

# Quickstart name → REST trigger config.
# demo: GET endpoint for problem data (None = server pre-loads its own data)
# solve: POST endpoint to start solving
QUICKSTARTS: dict[str, dict[str, str | None]] = {
    "bed-allocation":           {"demo": "/demo-data",         "solve": "/schedules"},
    "conference-scheduling":    {"demo": "/demo-data",         "solve": "/schedules"},
    "employee-scheduling":      {"demo": "/demo-data/SMALL",   "solve": "/schedules"},
    "facility-location":        {"demo": None,                 "solve": "/flp/solve"},
    "flight-crew-scheduling":   {"demo": "/demo-data",         "solve": "/schedules"},
    "food-packaging":           {"demo": None,                 "solve": "/schedule/solve"},
    "maintenance-scheduling":   {"demo": "/demo-data/SMALL",   "solve": "/schedules"},
    "meeting-scheduling":       {"demo": "/demo-data",         "solve": "/schedules"},
    "order-picking":            {"demo": None,                 "solve": "/orderPicking/solve"},
    "project-job-scheduling":   {"demo": "/demo-data",         "solve": "/schedules"},
    "school-timetabling":       {"demo": "/demo-data/SMALL",   "solve": "/timetables"},
    "sports-league-scheduling": {"demo": "/demo-data",         "solve": "/schedules"},
    "task-assigning":           {"demo": "/demo-data",         "solve": "/schedules"},
    "tournament-scheduling":    {"demo": "/demo-data",         "solve": "/schedules"},
    "vehicle-routing":          {"demo": "/demo-data/FIRENZE", "solve": "/route-plans"},
}

# Matches the solver-level summary line only (not per-phase summaries).
_SPEED_RE = re.compile(r"Solving ended:.*?move evaluation speed \((\d+)/sec\)")

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
) -> int | None:
    """Start server, trigger solving, capture speed, kill server. Returns moves/sec or None."""
    base_url = f"http://127.0.0.1:{port}"
    run_timeout = time_limit + 120  # generous margin beyond solver time
    jar = directory / "quarkus-run.jar"

    if not _wait_port_free(port):
        _log(f"[{name}] run {run_idx}/{total_runs}: ERR (port {port} still busy)")
        return None

    proc = subprocess.Popen(
        [
            "java",
            f"-Dquarkus.http.port={port}",
            f"-Dquarkus.timefold.solver.termination.spent-limit={time_limit}s",
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
    speed_box: list[int | None] = [None]

    def _reader() -> None:
        # Drain continuously; prevents stdout buffer from filling (which would block proc.wait()).
        for line in proc.stdout:
            if not ready_event.is_set() and "started in" in line:
                ready_event.set()
            if not speed_event.is_set():
                m = _SPEED_RE.search(line)
                if m:
                    speed_box[0] = int(m.group(1))
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

        # Fetch demo data (if needed) and trigger solving.
        demo_path = config["demo"]
        solve_path = config["solve"]
        if demo_path:
            with urllib.request.urlopen(f"{base_url}{demo_path}", timeout=30) as resp:
                body = resp.read()
            req = urllib.request.Request(
                f"{base_url}{solve_path}",
                data=body,
                headers={"Content-Type": "application/json"},
                method="POST",
            )
        else:
            req = urllib.request.Request(
                f"{base_url}{solve_path}",
                data=b"",
                method="POST",
            )
        with urllib.request.urlopen(req, timeout=30):
            pass  # async quickstarts return jobId; we don't need it

        # Wait for "Solving ended:" in server logs.
        if speed_event.wait(timeout=run_timeout):
            speed = speed_box[0]
            _log(f"[{name}] run {run_idx}/{total_runs}: {speed:,} moves/s")
            return speed

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


def _write_csv(name: str, speeds: list[int], total_runs: int, path: str) -> None:
    with open(path, "w", newline="") as f:
        w = csv.writer(f)
        w.writerow(["quickstart", "runs", "mean", "min", "max", "std_dev", "ci_lower", "ci_upper"])
        if speeds:
            mean = statistics.mean(speeds)
            std = f"{statistics.stdev(speeds):.0f}" if len(speeds) >= 2 else ""
            ci = _ci_bounds(speeds)
            ci_lower = f"{ci[0]:.0f}" if ci else ""
            ci_upper = f"{ci[1]:.0f}" if ci else ""
            w.writerow([name, len(speeds), f"{mean:.0f}", min(speeds), max(speeds), std, ci_lower, ci_upper])
        else:
            w.writerow([name, 0, "", "", "", "", "", ""])
    _log(f"CSV written to {path}")


def main() -> None:
    p = argparse.ArgumentParser(
        description="Benchmark a Timefold quickstart's move evaluation speed",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="Known quickstarts:\n  " + "\n  ".join(QUICKSTARTS),
    )
    p.add_argument("quickstart", metavar="QUICKSTART",
                   help="quickstart name (used to look up REST endpoints)")
    p.add_argument("directory", metavar="DIRECTORY", type=Path,
                   help="path to quarkus-app/ directory (must contain quarkus-run.jar)")
    p.add_argument("--runs", type=int, default=20, metavar="N",
                   help="number of runs (default: 20)")
    p.add_argument("--parallel", type=int, default=1, metavar="N",
                   help="concurrent server instances, each on its own port (default: 1)")
    p.add_argument("--time-limit", type=int, default=60, metavar="S",
                   help="solver time limit in seconds per run (default: 60)")
    p.add_argument("--base-port", type=int, default=8080, metavar="N",
                   help="first port in the pool; each parallel instance uses base-port+i (default: 8080)")
    p.add_argument("--output", default="benchmark-results.csv", metavar="FILE",
                   help="CSV output path (default: benchmark-results.csv)")
    args = p.parse_args()

    name = args.quickstart
    if name not in QUICKSTARTS:
        p.error(f"Unknown quickstart: {name!r}\nKnown: {', '.join(QUICKSTARTS)}")

    directory = args.directory.resolve()
    if not directory.is_dir():
        p.error(f"Directory not found: {directory}")
    jar = directory / "quarkus-run.jar"
    if not jar.is_file():
        p.error(f"JAR not found: {jar}\nExpected quarkus-app/ directory with quarkus-run.jar inside.")

    base_port = args.base_port
    _log(
        f"=== Benchmarking {name}: {args.runs} run(s)"
        f", parallel={args.parallel}, time-limit={args.time_limit}s ==="
    )
    _log(f"    Directory : {directory}")
    _log(f"    Ports     : {base_port}–{base_port + args.parallel - 1}\n")

    config = QUICKSTARTS[name]

    # Port pool: one slot per parallel instance, each on a distinct port.
    port_q: queue.Queue[int] = queue.Queue()
    for i in range(args.parallel):
        port_q.put(base_port + i)

    # Shared run counter: workers claim the next run index atomically.
    # abort event: set by any worker on failure; stops all remaining work.
    all_speeds: list[int] = []
    speeds_lock = threading.Lock()
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
                s = _run_once(name, config, directory, run_idx, args.runs, port, args.time_limit)
                if s is None:
                    abort.set()
                    break
                with speeds_lock:
                    all_speeds.append(s)
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

    _print_table(name, all_speeds, args.runs)
    _write_csv(name, all_speeds, args.runs, args.output)


if __name__ == "__main__":
    main()
