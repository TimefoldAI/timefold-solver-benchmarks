package ai.timefold.solver.benchmarks.micro.factorial.configuration;

import java.util.List;

import ai.timefold.solver.benchmarks.micro.factorial.planning.Level;
import ai.timefold.solver.benchmarks.micro.factorial.planning.Observation;
import ai.timefold.solver.core.config.solver.SolverConfig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConditionalConfiguration implements AbstractConfiguration {

    private final String key;
    private final List<ConditionalConfigurationValue> values;
    private AbstractConfiguration match = null;

    @JsonCreator
    public ConditionalConfiguration(@JsonProperty("key") String key,
            @JsonProperty("values") List<ConditionalConfigurationValue> values) {
        this.key = key;
        this.values = values;
    }

    @Override
    public Level getLevel() {
        if (match != null) {
            return match.getLevel();
        }
        return null;
    }

    @Override
    public String getFactorName() {
        return key;
    }

    @Override
    public void apply(Observation observation, SolverConfig solverConfig) {
        if (match != null) {
            match.apply(observation, solverConfig);
        }
    }

    @Override
    public void init(Observation observation) {
        values.stream()
                .filter(v -> v.conditions().stream().allMatch(c -> {
                    var level = observation.getValue(c.requiredFactor());
                    return level != null && level.equals(c.requiredLevel());
                }))
                .findFirst()
                .ifPresent(matchedValue -> this.match = switch (matchedValue.type()) {
                    case "ReadOnly" -> new ReadOnlyConfiguration(key, matchedValue.value());
                    case "Observation" -> new ObservationConfiguration(key, matchedValue.value());
                    default ->
                        throw new IllegalStateException("Unexpected conditional type : %s".formatted(matchedValue.type()));
                });
    }

    @Override
    public AbstractConfiguration copy() {
        return new ConditionalConfiguration(key, values);
    }

    @Override
    public String toCSV() {
        if (match != null) {
            return match.toCSV();
        }
        return null;
    }

    @Override
    public String toString() {
        if (match != null) {
            return match.toString();
        }
        return "-";
    }

}
