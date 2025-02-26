#!/bin/bash
mkdir -p experiment
rm experiment/nohup*
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388200541 > ./experiment/nohup1.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388173750 > ./experiment/nohup2.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388170916 > ./experiment/nohup3.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388200541 > ./experiment/nohup4.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388191666 > ./experiment/nohup5.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388155458 > ./experiment/nohup6.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388178541 > ./experiment/nohup7.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388196125 > ./experiment/nohup8.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388205000 > ./experiment/nohup9.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388191666 > ./experiment/nohup10.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388196125 > ./experiment/nohup11.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388170916 > ./experiment/nohup12.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388193916 > ./experiment/nohup13.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388186250 > ./experiment/nohup14.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388181291 > ./experiment/nohup15.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388188541 > ./experiment/nohup16.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388164041 > ./experiment/nohup17.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388198333 > ./experiment/nohup18.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388161541 > ./experiment/nohup19.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388173750 > ./experiment/nohup20.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388166250 > ./experiment/nohup21.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388184041 > ./experiment/nohup22.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388205000 > ./experiment/nohup23.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388186250 > ./experiment/nohup24.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388175958 > ./experiment/nohup25.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388181291 > ./experiment/nohup26.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388168666 > ./experiment/nohup27.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388166250 > ./experiment/nohup28.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388155458 > ./experiment/nohup29.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388202750 > ./experiment/nohup30.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388184041 > ./experiment/nohup31.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388198333 > ./experiment/nohup32.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388161541 > ./experiment/nohup33.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388164041 > ./experiment/nohup34.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388188541 > ./experiment/nohup35.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388175958 > ./experiment/nohup36.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388202750 > ./experiment/nohup37.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-main.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388193916 > ./experiment/nohup38.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388178541 > ./experiment/nohup39.out 2>&1
nohup java -Xmx4g -XX:+UseParallelGC -cp ./experiment/benchmarks-restart.jar ai.timefold.solver.benchmarks.competitive.cvrplib.Main ENTERPRISE_EDITION 400 77308388168666 > ./experiment/nohup40.out 2>&1
sudo shutdown -h now