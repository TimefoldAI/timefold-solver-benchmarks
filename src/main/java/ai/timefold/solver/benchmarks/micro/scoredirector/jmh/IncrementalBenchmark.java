package ai.timefold.solver.benchmarks.micro.scoredirector.jmh;

import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;

import org.openjdk.jmh.annotations.Param;

public class IncrementalBenchmark extends AbstractBenchmark {

    @Param
    public Example incrementalExample;

    @Override
    protected ScoreDirectorType getScoreDirectorType() {
        return ScoreDirectorType.INCREMENTAL;
    }

    @Override
    protected Example getExample() {
        return incrementalExample;
    }
}
