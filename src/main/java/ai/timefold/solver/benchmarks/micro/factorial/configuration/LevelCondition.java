package ai.timefold.solver.benchmarks.micro.factorial.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LevelCondition(@JsonProperty("requiredFactor") String requiredFactor,
                             @JsonProperty("requiredLevel") String requiredLevel) {
}
