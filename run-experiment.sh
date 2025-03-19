#!/bin/bash

sh run-experiment-small.sh
mkdir -p results/small
rm results/small/*
mv experiment/nohup* results/small
mv result/CVRPLIB* results/small

sh run-experiment-medium.sh
mkdir -p results/medium
rm -f results/medium/*
mv experiment/nohup* results/medium
mv result/CVRPLIB* results/medium

sh run-experiment-large.sh
mkdir -p results/large
rm -f results/large/*
mv experiment/nohup* results/large
mv result/CVRPLIB* results/large

sudo shutdown -h now