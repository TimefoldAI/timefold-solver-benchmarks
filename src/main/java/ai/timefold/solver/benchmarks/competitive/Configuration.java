package ai.timefold.solver.benchmarks.competitive;

import ai.timefold.solver.core.config.solver.SolverConfig;

public interface Configuration<Dataset_ extends Dataset<Dataset_>> {

    SolverConfig getSolverConfig(Dataset_ dataset);

    String name();

}
