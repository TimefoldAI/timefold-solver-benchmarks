package ai.timefold.solver.benchmarks.jmh.coldstart.benchmarks;

import ai.timefold.solver.benchmarks.jmh.coldstart.problems.Example;

import org.openjdk.jmh.annotations.Param;

public class TimeToSolverFactoryBenchmark extends AbstractBenchmark {

    @Param
    public Example example;

    @Override
    protected Example getExample() {
        return example;
    }

}
