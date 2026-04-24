#!/bin/bash
BASE_DIR=$(pwd)
SOLVE_TIME=2m
#TIMEOUT_SECONDS=100

declare -A DEMO_DATA
declare -A SOLVE_ENDPOINT

SOLVE_ENDPOINT[school-timetabling]="timetables"
DEMO_DATA[school-timetabling]="/SMALL"

SOLVE_ENDPOINT[bed-allocation]="schedules"
DEMO_DATA[bed-allocation]=""

SOLVE_ENDPOINT[conference-scheduling]="schedules"
DEMO_DATA[conference-scheduling]=""

SOLVE_ENDPOINT[employee-scheduling]="schedules"
DEMO_DATA[employee-scheduling]="/LARGE"

#SOLVE_ENDPOINT[facility-location]="flp/solve"
#DEMO_DATA[facility-location]="MISSING"

SOLVE_ENDPOINT[flight-crew-scheduling]="schedules"
DEMO_DATA[flight-crew-scheduling]=""

#SOLVE_ENDPOINT[food-packaging]="schedule"
#DEMO_DATA[food-packaging]="MISSING"

SOLVE_ENDPOINT[maintenance-scheduling]="schedules"
DEMO_DATA[maintenance-scheduling]="/LARGE"

SOLVE_ENDPOINT[meeting-scheduling]="schedules"
DEMO_DATA[meeting-scheduling]=""

#SOLVE_ENDPOINT[order-picking]="orderPicking"
#DEMO_DATA[order-picking]="MISSING"

SOLVE_ENDPOINT[project-job-scheduling]="schedules"
DEMO_DATA[project-job-scheduling]=""

SOLVE_ENDPOINT[sports-league-scheduling]="schedules"
DEMO_DATA[sports-league-scheduling]=""

SOLVE_ENDPOINT[task-assigning]="schedules"
DEMO_DATA[task-assigning]=""

SOLVE_ENDPOINT[tournament-scheduling]="schedules"
DEMO_DATA[tournament-scheduling]=""

SOLVE_ENDPOINT[vehicle-routing]="route-plans"
DEMO_DATA[vehicle-routing]="/FIRENZE"

RANDOM_SEEDS=(0 1 42)
MOVE_THREAD_COUNTS=(NONE 2 4)

quickstart_dir() {
    echo "$BASE_DIR/timefold-quickstarts/java/$1"
}

quickstart_jar() {
    echo "$(quickstart_dir $1)/target/quarkus-app/quarkus-run.jar"
}

run_quickstart() {
  # Set $BASE_URL to override the default http://localhost:8080
  cd $(quickstart_dir $1)
  mvn clean package -Denterprise -DskipTests 1>&2 #&> /dev/null
  if [ $? -ne 0 ]
  then
      echo "Build failed"
      return 1
  fi
  nohup java "-Dquarkus.timefold.solver.termination.spent-limit=${SOLVE_TIME}" "-Dquarkus.timefold.solver.random-seed=${RANDOM_SEED}" "-Dquarkus.timefold.solver.move-thread-count=${MOVE_THREAD_COUNT}" -jar $(quickstart_jar $1) &> /dev/null &
  sleep 5
  BASE_URL="${BASE_URL:-http://localhost:8080}"
  # Step 1: GET /demo-data/SMALL
  PROBLEM=$(curl -sf "$BASE_URL/demo-data/${DEMO_DATA[$1]}")

  # Step 2: POST /$SOLVE_ENDPOINT to start solving
  JOB_ID=$(echo "$PROBLEM" | curl -sf -X POST \
    -H "Content-Type: application/json" \
    --data @- \
    "$BASE_URL/${SOLVE_ENDPOINT[$1]}")

  # Step 3: Poll /$SOLVE_ENDPOINT/{jobId}/status until solverStatus == "NOT_SOLVING"
  POLL_INTERVAL_MS=500
  POLL_INTERVAL_SECONDS=0.5
  START_TIME=$(date +%s)

  while true; do
    #CURRENT_TIME=$(date +%s)
    #ELAPSED=$((CURRENT_TIME - START_TIME))
    #if [ "$ELAPSED" -ge "$TIMEOUT_SECONDS" ]; then
    #  kill $!
    #  return 1
    #fi
    SOLVER_STATUS=$(curl -sf "$BASE_URL/${SOLVE_ENDPOINT[$1]}/$JOB_ID/status" | jq -r '.solverStatus')

    if [ "$SOLVER_STATUS" = "NOT_SOLVING" ]; then
      break
    fi

    sleep "$POLL_INTERVAL_SECONDS"
  done

  # Step 4: GET /$SOLVE_ENDPOINT/{jobId} to retrieve the solution
  SOLUTION=$(curl -sf "$BASE_URL/${SOLVE_ENDPOINT[$1]}/$JOB_ID")

  # Step 5: Assert solution is not null
  if [ -z "$SOLUTION" ]; then
    kill $!
    return 1
  fi

  SCORE=$(echo "$SOLUTION" | jq -r '.score')
  kill $!
  echo "$SCORE"
}

case $1 in
    1\.x)
    COMMUNITY="1.x"
    ENTERPRISE="1.x"
    QUICKSTART="development-1.x"
    ;;
    2\.x)
    COMMUNITY=main
    ENTERPRISE=main
    QUICKSTART=development
    ;;
    split)
    COMMUNITY=perf/split-random
    ENTERPRISE=perf/split-random
    QUICKSTART=development
    ;;
    *)
    echo "Usage: $0 1.x|2.x|split"
    exit 1
    ;;
esac

if [ -n "$USE_SSH" ]
then
    ENTERPRISE_URL=git@github.com:TimefoldAI/timefold-solver-enterprise.git
else
    ENTERPRISE_URL=https://github.com/TimefoldAI/timefold-solver-enterprise.git
fi

if [ -z "$SKIP_CLONE" ]
then
    rm -rf timefold-solver
    rm -rf timefold-solver-enterprise
    rm -rf timefold-quickstarts
    git clone --depth 1 --branch $COMMUNITY https://github.com/Christopher-Chianelli/timefold-solver.git
    git clone --depth 1 --branch $ENTERPRISE $ENTERPRISE_URL
    git clone --depth 1 --branch $QUICKSTART https://github.com/TimefoldAI/timefold-quickstarts.git
fi

echo "Building Solver and Solver Enterprise for branch $1"
cd timefold-solver
mvn clean install -Dquickly &> /dev/null
cd ..
cd timefold-solver-enterprise
mvn clean install -Dquickly &> /dev/null
cd ..

echo "Quickstart,RandomSeed,MoveThreadCount,Score" > "$1".csv
for quickstart in bed-allocation conference-scheduling employee-scheduling \
                  flight-crew-scheduling maintenance-scheduling meeting-scheduling \
                  project-job-scheduling school-timetabling sports-league-scheduling \
                  task-assigning tournament-scheduling vehicle-routing
do
    for RANDOM_SEED in "${RANDOM_SEEDS[@]}"; do
        for MOVE_THREAD_COUNT in "${MOVE_THREAD_COUNTS[@]}"; do
            echo "Benchmarking Quickstart $quickstart (seed=$RANDOM_SEED, threads=$MOVE_THREAD_COUNT)"
            echo "$quickstart,$RANDOM_SEED,$MOVE_THREAD_COUNT,$(run_quickstart $quickstart)" >> "$1".csv
        done
    done
done

echo "Finished Benchmarking for $1"
echo "Results (stored in $1.csv)"
cat "$1".csv
