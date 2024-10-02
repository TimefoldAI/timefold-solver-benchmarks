package ai.timefold.solver.benchmarks.micro.factorial.configuration;

import ai.timefold.solver.core.config.solver.SolverConfig;

public class DummyConfiguration extends SingleConfiguration {

    public DummyConfiguration(String key, Object value) {
        super(key, value);
    }

    @Override
    public void apply(SolverConfig solverConfig) {
        // Do nothing
    }
}
