#!/bin/bash
mkdir -p experiment
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 400 > ./experiment/nohup1.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 100 > ./experiment/nohup2.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 > ./experiment/nohup3.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 > ./experiment/nohup4.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 1000 > ./experiment/nohup5.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 100 > ./experiment/nohup6.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 1000 > ./experiment/nohup7.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 1000 > ./experiment/nohup8.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 1000 > ./experiment/nohup9.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 100 > ./experiment/nohup10.out 2>&1

nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 100 > ./experiment/nohup11.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 1000 > ./experiment/nohup12.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 1000 > ./experiment/nohup13.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 > ./experiment/nohup14.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 100 > ./experiment/nohup15.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 1000 > ./experiment/nohup16.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 100 > ./experiment/nohup17.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 1000 > ./experiment/nohup18.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 100 > ./experiment/nohup19.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 1000 > ./experiment/nohup20.out 2>&1

nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 1000 > ./experiment/nohup21.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 100 > ./experiment/nohup22.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 > ./experiment/nohup23.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 400 > ./experiment/nohup24.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 > ./experiment/nohup25.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 400 > ./experiment/nohup26.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 400 > ./experiment/nohup27.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 100 > ./experiment/nohup28.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 100 > ./experiment/nohup29.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 1000 > ./experiment/nohup30.out 2>&1

nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 100 > ./experiment/nohup31.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 100 > ./experiment/nohup32.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 400 > ./experiment/nohup33.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 400 > ./experiment/nohup34.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 > ./experiment/nohup35.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 1000 > ./experiment/nohup36.out 2>&1

sudo shutdown -h now
