package ai.timefold.solver.benchmarks.jmh.scoredirector.benchmarks;

import ai.timefold.solver.benchmarks.jmh.scoredirector.Example;
import ai.timefold.solver.benchmarks.jmh.scoredirector.ScoreDirectorType;

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
