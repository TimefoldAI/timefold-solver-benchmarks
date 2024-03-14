package ai.timefold.solver.benchmarks.micro.scoredirector.jmh;

import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;

import org.openjdk.jmh.annotations.Param;

public class ConstraintStreamsBenchmark extends AbstractBenchmark {

    @Param
    public Example csExample;

    @Override
    protected ScoreDirectorType getScoreDirectorType() {
        return ScoreDirectorType.CONSTRAINT_STREAMS;
    }

    @Override
    protected Example getExample() {
        return csExample;
    }
}
