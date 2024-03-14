package ai.timefold.solver.benchmarks.micro.scoredirector.jmh;

import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;

import org.openjdk.jmh.annotations.Param;

public class ConstraintStreamsJustifiedBenchmark extends AbstractBenchmark {

    @Param
    public Example csJustifiedExample;

    @Override
    protected ScoreDirectorType getScoreDirectorType() {
        return ScoreDirectorType.CONSTRAINT_STREAMS_JUSTIFIED;
    }

    @Override
    protected Example getExample() {
        return csJustifiedExample;
    }
}
