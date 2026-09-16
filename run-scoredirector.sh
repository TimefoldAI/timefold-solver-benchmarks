#!/bin/bash
sudo -i sysctl kernel.perf_event_paranoid=1
sudo -i sysctl kernel.kptr_restrict=0
# JAVA_HOME picks the JDK, instead of whichever java happens to come first on the PATH;
# JMH forks its children from the host process, so pinning the host pins every fork.
# BENCHMARK_JAR lets the caller alternate between two binaries without copying a file
# between measurements. Both default to what a plain local run expects.
"${JAVA_HOME:+$JAVA_HOME/bin/}java" -cp "${BENCHMARK_JAR:-target/benchmarks.jar}" \
  ai.timefold.solver.benchmarks.micro.scoredirector.Main "$@"
