#!/bin/bash
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 400 > /tmp/nohup1.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 100 > /tmp/nohup2.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 > /tmp/nohup3.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 > /tmp/nohup4.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 1000 > /tmp/nohup5.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 100 > /tmp/nohup6.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 1000 > /tmp/nohup7.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 1000 > /tmp/nohup8.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 1000 > /tmp/nohup9.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 100 > /tmp/nohup10.out 2>&1

nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 100 > /tmp/nohup11.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 1000 > /tmp/nohup12.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 1000 > /tmp/nohup13.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 > /tmp/nohup14.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 100 > /tmp/nohup15.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 1000 > /tmp/nohup16.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 100 > /tmp/nohup17.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 1000 > /tmp/nohup18.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 100 > /tmp/nohup19.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 1000 > /tmp/nohup20.out 2>&1

nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 1000 > /tmp/nohup21.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 100 > /tmp/nohup22.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 > /tmp/nohup23.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 400 > /tmp/nohup24.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 > /tmp/nohup25.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 400 > /tmp/nohup26.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 400 > /tmp/nohup27.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 100 > /tmp/nohup28.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 100 > /tmp/nohup29.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 1000 > /tmp/nohup30.out 2>&1

nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 100 > /tmp/nohup31.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 100 > /tmp/nohup32.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 400 > /tmp/nohup33.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 400 > /tmp/nohup34.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 > /tmp/nohup35.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp /tmp/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main COMMUNITY_EDITION 1000 > /tmp/nohup36.out 2>&1

sudo shutdown -h now
