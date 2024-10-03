package ai.timefold.solver.benchmarks.micro.factorial.configuration;

import ai.timefold.solver.benchmarks.micro.factorial.planning.Level;
import ai.timefold.solver.benchmarks.micro.factorial.planning.Observation;
import ai.timefold.solver.core.config.solver.SolverConfig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ObservationConfiguration extends AbstractConfiguration {

    @JsonCreator
    public ObservationConfiguration(@JsonProperty("key") String key, @JsonProperty("value") String value) {
        super(key, value);
    }

    public ObservationConfiguration(Level level) {
        super(level);
    }

    @Override
    public String toCSV() {
        return getLevel() != null && getLevel().getValue() != null ? getLevel().getValue() : "-";
    }

    @Override
    public void apply(Observation observation, SolverConfig solverConfig) {
        getLevel().apply(observation, solverConfig);
    }
}
