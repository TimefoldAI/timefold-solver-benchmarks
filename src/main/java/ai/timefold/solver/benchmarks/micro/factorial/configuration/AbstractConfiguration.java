package ai.timefold.solver.benchmarks.micro.factorial.configuration;

import java.util.List;
import java.util.Objects;

import ai.timefold.solver.benchmarks.micro.factorial.planning.Factor;
import ai.timefold.solver.benchmarks.micro.factorial.planning.Level;
import ai.timefold.solver.benchmarks.micro.factorial.planning.Observation;
import ai.timefold.solver.core.config.solver.SolverConfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ObservationConfiguration.class, name = "Observation"),
        @JsonSubTypes.Type(value = ExperimentConfiguration.class, name = "Experiment"),
        @JsonSubTypes.Type(value = ConditionalConfiguration.class, name = "Conditional") })
public abstract class AbstractConfiguration {

    private final Level level;

    protected AbstractConfiguration(String key, String value) {
        this(new Factor(key, List.of(Objects.requireNonNull(value))).getLevelList().getFirst());
    }

    protected AbstractConfiguration(Level level) {
        this.level = Objects.requireNonNull(level);
    }

    public Level getLevel() {
        return level;
    }

    public abstract void apply(Observation observation, SolverConfig solverConfig);

    public abstract String toCSV();

    @Override
    public String toString() {
        return "AbstractConfiguration{" +
                level.toString() +
                '}';
    }
}
