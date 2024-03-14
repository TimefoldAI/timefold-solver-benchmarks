package ai.timefold.solver.benchmarks.micro.coldstart.jmh;

import ai.timefold.solver.benchmarks.micro.coldstart.problems.Example;

import org.openjdk.jmh.annotations.Param;

public class TimeToFirstScoreBenchmark extends AbstractBenchmark {

    @Param
    public Example example;

    @Override
    protected Example getExample() {
        return example;
    }

}
