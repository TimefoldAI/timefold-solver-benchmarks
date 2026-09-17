#!/bin/bash
# The async profiler needs kernel.perf_event_paranoid at 1 or less and kernel.kptr_restrict at 0.
# Setting them belongs to whoever prepares the machine, once - see "Prepare the machine for
# profiling" in performance_score_director.yml - not to this script, which runs once for every fork.
# JAVA_HOME picks the JDK, instead of whichever java happens to come first on the PATH;
# JMH forks its children from the host process, so pinning the host pins every fork.
# BENCHMARK_JAR lets the caller alternate between two binaries without copying a file
# between measurements. Both default to what a plain local run expects.
"${JAVA_HOME:+$JAVA_HOME/bin/}java" -cp "${BENCHMARK_JAR:-target/benchmarks.jar}" \
  ai.timefold.solver.benchmarks.micro.scoredirector.Main "$@"
