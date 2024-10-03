package ai.timefold.solver.benchmarks.micro.factorial.configuration;

import java.util.List;

import ai.timefold.solver.benchmarks.micro.factorial.planning.Level;
import ai.timefold.solver.benchmarks.micro.factorial.planning.Observation;
import ai.timefold.solver.core.config.solver.SolverConfig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConditionalConfiguration extends ObservationConfiguration {

    private final List<LevelCondition> levelConditionList;

    @JsonCreator
    public ConditionalConfiguration(@JsonProperty("key") String key, @JsonProperty("value") String value,
                                    @JsonProperty("conditions") List<LevelCondition> conditions) {
        super(key, value);
        this.levelConditionList = conditions;
    }

    public ConditionalConfiguration(Level level, List<LevelCondition> conditions) {
        super(level);
        this.levelConditionList = conditions;
    }

    @Override
    public void apply(Observation observation, SolverConfig solverConfig) {
        var allMatch = levelConditionList.stream().allMatch(c -> {
            var level = observation.getValue(c.requiredFactor());
            return level != null && level.equals(c.requiredLevel());
        });
        if (allMatch) {
            super.apply(observation, solverConfig);
        }
    }
}
