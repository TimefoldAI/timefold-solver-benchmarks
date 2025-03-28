package ai.timefold.solver.benchmarks.micro.scoredirector;

import java.util.Objects;

import ai.timefold.solver.benchmarks.micro.scoredirector.jmh.AbstractBenchmark;
import ai.timefold.solver.benchmarks.micro.scoredirector.jmh.ConstraintStreamsBenchmark;
import ai.timefold.solver.benchmarks.micro.scoredirector.jmh.ConstraintStreamsJustifiedBenchmark;
import ai.timefold.solver.benchmarks.micro.scoredirector.jmh.EasyBenchmark;
import ai.timefold.solver.benchmarks.micro.scoredirector.jmh.IncrementalBenchmark;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.core.impl.score.director.ScoreDirectorFactory;
import ai.timefold.solver.core.impl.score.director.ScoreDirectorFactoryFactory;

/**
 * Order by expected speed increase.
 */
public enum ScoreDirectorType implements Comparable<ScoreDirectorType> {

    EASY(EasyBenchmark.class, "easyExample"),
    CONSTRAINT_STREAMS_JUSTIFIED(ConstraintStreamsJustifiedBenchmark.class, "csJustifiedExample"),
    CONSTRAINT_STREAMS(ConstraintStreamsBenchmark.class, "csExample"),
    INCREMENTAL(IncrementalBenchmark.class, "incrementalExample");

    private final Class<? extends AbstractBenchmark> benchmarkClass;
    private final String benchmarkParamName;

    ScoreDirectorType(Class<? extends AbstractBenchmark> benchmarkClass, String benchmarkParamName) {
        this.benchmarkClass = Objects.requireNonNull(benchmarkClass);
        this.benchmarkParamName = benchmarkParamName;
    }

    public Class<? extends AbstractBenchmark> getBenchmarkClass() {
        return benchmarkClass;
    }

    public String getBenchmarkParamName() {
        return benchmarkParamName;
    }

    public static <Solution_, Score_ extends Score<Score_>> ScoreDirectorFactory<Solution_, Score_> buildScoreDirectorFactory(
            ScoreDirectorFactoryConfig scoreDirectorFactoryConfig, SolutionDescriptor<Solution_> solutionDescriptor) {
        var scoreDirectorFactoryFactory = new ScoreDirectorFactoryFactory<Solution_, Score_>(scoreDirectorFactoryConfig);
        return scoreDirectorFactoryFactory.buildScoreDirectorFactory(EnvironmentMode.PHASE_ASSERT, solutionDescriptor);
    }

}
