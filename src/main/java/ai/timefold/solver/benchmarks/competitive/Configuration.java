package ai.timefold.solver.benchmarks.competitive;

import java.time.Duration;

import ai.timefold.solver.core.config.solver.SolverConfig;

public interface Configuration<Dataset_ extends Dataset<Dataset_>> {

    SolverConfig getSolverConfig(Dataset_ dataset);

    default Duration getMaximumDurationPerDataset() {
        return Duration.ofSeconds(AbstractCompetitiveBenchmark.MAX_SECONDS);
    }

    String name();

    boolean usesEnterprise();

}
