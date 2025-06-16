package ai.timefold.solver.benchmarks.micro.cloning.jmh;

import ai.timefold.solver.benchmarks.micro.cloning.problems.Example;

import org.openjdk.jmh.annotations.Param;

public class CloningBenchmark extends AbstractBenchmark {

    @Param
    public Example example;

    @Override
    protected Example getExample() {
        return example;
    }

}
