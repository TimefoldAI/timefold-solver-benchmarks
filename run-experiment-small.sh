#!/bin/bash
mkdir -p experiment
rm experiment/nohup*
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904777625 > ./experiment/nohup1.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904769750 > ./experiment/nohup2.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904773750 > ./experiment/nohup3.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904783250 > ./experiment/nohup4.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904759875 > ./experiment/nohup5.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904744458 > ./experiment/nohup6.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904757791 > ./experiment/nohup7.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904775583 > ./experiment/nohup8.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904765791 > ./experiment/nohup9.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904769750 > ./experiment/nohup10.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904763625 > ./experiment/nohup11.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904783250 > ./experiment/nohup12.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904732833 > ./experiment/nohup13.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904759875 > ./experiment/nohup14.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904732833 > ./experiment/nohup15.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904771833 > ./experiment/nohup16.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904748291 > ./experiment/nohup17.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904767875 > ./experiment/nohup18.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904771833 > ./experiment/nohup19.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904767875 > ./experiment/nohup20.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904763625 > ./experiment/nohup21.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904757791 > ./experiment/nohup22.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904785125 > ./experiment/nohup23.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904748291 > ./experiment/nohup24.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904761750 > ./experiment/nohup25.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904761750 > ./experiment/nohup26.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904746416 > ./experiment/nohup27.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904781416 > ./experiment/nohup28.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904777625 > ./experiment/nohup29.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904746416 > ./experiment/nohup30.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904785125 > ./experiment/nohup31.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904781416 > ./experiment/nohup32.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904773750 > ./experiment/nohup33.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904779583 > ./experiment/nohup34.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904779583 > ./experiment/nohup35.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904742416 > ./experiment/nohup36.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904765791 > ./experiment/nohup37.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904744458 > ./experiment/nohup38.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904742416 > ./experiment/nohup39.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 200 77961904775583 > ./experiment/nohup40.out 2>&1
#sudo shutdown -h now