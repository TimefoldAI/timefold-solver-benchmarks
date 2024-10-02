package ai.timefold.solver.benchmarks.micro.factorial.configuration;

import ai.timefold.solver.core.config.solver.SolverConfig;

public abstract class AbstractConfiguration {

    public abstract String getKey();

    public abstract Object getValue();

    public abstract void apply(SolverConfig solverConfig);

    public abstract String toCSV();
}
