#!/bin/bash
# The async profiler needs perf_event_paranoid at 1 or less to open perf events, and kptr_restrict
# at 0 to resolve kernel symbols. Without them it falls back to its ctimer engine, which samples by
# signal instead, and records no kernel frames.
# Read the current value first, so a machine that sets these at boot needs no sudo at all, and so
# this stays quiet when there is nothing to do - this script runs once for every fork.
# "sudo -n" fails immediately rather than waiting for a password that no CI job can type.
ensure_sysctl() { # key, wanted value, test
  CURRENT=$(cat "/proc/sys/${1//.//}" 2>/dev/null || echo "unknown")
  if [ "$CURRENT" != "unknown" ] && [ "$CURRENT" -"$3" "$2" ] 2>/dev/null; then
    return 0
  fi
  sudo -n sysctl -q "$1=$2" 2>/dev/null && return 0
  echo "WARNING: $1 is $CURRENT, not $2, and it could not be changed." >&2
  echo "WARNING: The profiler will fall back to its ctimer engine and record no kernel symbols." >&2
}
ensure_sysctl kernel.perf_event_paranoid 1 le
ensure_sysctl kernel.kptr_restrict 0 eq
# JAVA_HOME picks the JDK, instead of whichever java happens to come first on the PATH;
# JMH forks its children from the host process, so pinning the host pins every fork.
# BENCHMARK_JAR lets the caller alternate between two binaries without copying a file
# between measurements. Both default to what a plain local run expects.
"${JAVA_HOME:+$JAVA_HOME/bin/}java" -cp "${BENCHMARK_JAR:-target/benchmarks.jar}" \
  ai.timefold.solver.benchmarks.micro.scoredirector.Main "$@"
