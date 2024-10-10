package ai.timefold.solver.benchmarks.micro.factorial.configuration;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConditionalConfigurationValue(@JsonProperty("type") String type, @JsonProperty("value") String value,
        @JsonProperty("conditions") List<LevelCondition> conditions) {
}
