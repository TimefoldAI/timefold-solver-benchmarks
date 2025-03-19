#!/bin/bash

mkdir -p experiment/1
rm experiment/1/nohup*
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 100 77961904777625 > ./experiment/1/nohup1.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904777625 > ./experiment/1/nohup2.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77961904777625 > ./experiment/1/nohup3.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77961904777625 > ./experiment/1/nohup4.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 800 77961904777625 > ./experiment/1/nohup5.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 1000 77961904777625 > ./experiment/1/nohup6.out 2>&1

mkdir -p experiment/2
rm experiment/2/nohup*
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 100 77961904777625 > ./experiment/2/nohup1.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904777625 > ./experiment/2/nohup2.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77961904777625 > ./experiment/2/nohup3.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77961904777625 > ./experiment/2/nohup4.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 800 77961904777625 > ./experiment/2/nohup5.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 1000 77961904777625 > ./experiment/2/nohup6.out 2>&1

mkdir experiment/3
rm experiment/3/nohup*
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 100 77961904777625 > ./experiment/3/nohup1.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 200 77961904777625 > ./experiment/3/nohup2.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 400 77961904777625 > ./experiment/3/nohup3.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 600 77961904777625 > ./experiment/3/nohup4.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 800 77961904777625 > ./experiment/3/nohup5.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 1000 77961904777625 > ./experiment/3/nohup6.out 2>&1

mkdir -p experiment/4
rm experiment/4/nohup*
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 100 77961904777625 > ./experiment/4/nohup1.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 200 77961904777625 > ./experiment/4/nohup2.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 400 77961904777625 > ./experiment/4/nohup3.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 600 77961904777625 > ./experiment/4/nohup4.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 800 77961904777625 > ./experiment/4/nohup5.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 1000 77961904777625 > ./experiment/4/nohup6.out 2>&1