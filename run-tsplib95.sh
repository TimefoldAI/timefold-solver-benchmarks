#!/bin/bash
nohup java -Xmx4g -XX:+UseParallelGC -cp target/benchmarks.jar ai.timefold.solver.benchmarks.competitive.tsplib95.Main > target/nohup.out 2>&1 &