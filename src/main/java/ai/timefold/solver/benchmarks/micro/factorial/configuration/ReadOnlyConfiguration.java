package ai.timefold.solver.benchmarks.micro.factorial.configuration;

import ai.timefold.solver.benchmarks.micro.factorial.planning.Observation;
import ai.timefold.solver.core.config.solver.SolverConfig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadOnlyConfiguration extends ObservationConfiguration {

    @JsonCreator
    public ReadOnlyConfiguration(@JsonProperty("key") String key, @JsonProperty("value") String value) {
        super(key, value);
    }

    @Override
    public void apply(Observation observation, SolverConfig solverConfig) {
        // Do nothing
    }
}
