package ai.timefold.solver.benchmarks.micro.scoredirector.jmh;

import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;

import org.openjdk.jmh.annotations.Param;

public class EasyBenchmark extends AbstractBenchmark {

    @Param
    public Example easyExample;

    @Override
    protected ScoreDirectorType getScoreDirectorType() {
        return ScoreDirectorType.EASY;
    }

    @Override
    protected Example getExample() {
        return easyExample;
    }
}
