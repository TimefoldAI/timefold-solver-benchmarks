#!/bin/bash
mkdir -p experiment
rm experiment/nohup*
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220558458 > ./experiment/nohup1.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220524000 > ./experiment/nohup2.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220537416 > ./experiment/nohup3.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220542333 > ./experiment/nohup4.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220558458 > ./experiment/nohup5.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220532958 > ./experiment/nohup6.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220537416 > ./experiment/nohup7.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220539916 > ./experiment/nohup8.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220544541 > ./experiment/nohup9.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220526250 > ./experiment/nohup10.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220556250 > ./experiment/nohup11.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220539916 > ./experiment/nohup12.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220551458 > ./experiment/nohup13.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220506625 > ./experiment/nohup14.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220530708 > ./experiment/nohup15.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220546833 > ./experiment/nohup16.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220556250 > ./experiment/nohup17.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220553625 > ./experiment/nohup18.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220546833 > ./experiment/nohup19.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220512708 > ./experiment/nohup20.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220512708 > ./experiment/nohup21.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220524000 > ./experiment/nohup22.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220530708 > ./experiment/nohup23.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220563333 > ./experiment/nohup24.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220561083 > ./experiment/nohup25.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220532958 > ./experiment/nohup26.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220528500 > ./experiment/nohup27.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220561083 > ./experiment/nohup28.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220549041 > ./experiment/nohup29.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220535250 > ./experiment/nohup30.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220563333 > ./experiment/nohup31.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220506625 > ./experiment/nohup32.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220549041 > ./experiment/nohup33.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220535250 > ./experiment/nohup34.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220528500 > ./experiment/nohup35.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220544541 > ./experiment/nohup36.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220553625 > ./experiment/nohup37.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220542333 > ./experiment/nohup38.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220551458 > ./experiment/nohup39.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 600 77322220526250 > ./experiment/nohup40.out 2>&1
#sudo shutdown -h now