package ai.timefold.solver.benchmarks.micro.factorial.configuration;

import java.util.List;
import java.util.Objects;

import ai.timefold.solver.benchmarks.micro.factorial.planning.Factor;
import ai.timefold.solver.benchmarks.micro.factorial.planning.Level;
import ai.timefold.solver.benchmarks.micro.factorial.planning.Observation;
import ai.timefold.solver.core.config.solver.SolverConfig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ObservationConfiguration implements AbstractConfiguration {

    private final Level level;

    @JsonCreator
    public ObservationConfiguration(@JsonProperty("key") String key, @JsonProperty("value") String value) {
        this(new Factor(key, List.of(Objects.requireNonNull(value))).getLevelList().getFirst());
    }

    public ObservationConfiguration(Level level) {
        this.level = level;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public String getFactorName() {
        if (level != null) {
            return level.getFactorName();
        }
        return null;
    }

    @Override
    public void apply(Observation observation, SolverConfig solverConfig) {
        if (level != null) {
            getLevel().apply(observation, solverConfig);
        }
    }

    @Override
    public void init(Observation observation) {
        // Do nothing
    }

    @Override
    public AbstractConfiguration copy() {
        return new ObservationConfiguration(level.getFactorName(), level.getValue());
    }

    @Override
    public String toCSV() {
        return getLevel() != null && getLevel().getValue() != null ? getLevel().getValue() : "-";
    }

    @Override
    public String toString() {
        return "Configuration{%s}".formatted(level.toString());
    }
}
