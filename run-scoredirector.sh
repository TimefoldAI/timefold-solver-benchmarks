#!/bin/bash
sudo -i sysctl kernel.perf_event_paranoid=1
sudo -i sysctl kernel.kptr_restrict=0
taskset -c 0 java -cp target/benchmarks.jar ai.timefold.solver.benchmarks.micro.scoredirector.Main