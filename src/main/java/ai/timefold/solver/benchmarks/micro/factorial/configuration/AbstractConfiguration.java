package ai.timefold.solver.benchmarks.micro.factorial.configuration;

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
        @JsonSubTypes.Type(value = ReadOnlyConfiguration.class, name = "ReadOnly"),
        @JsonSubTypes.Type(value = ConditionalConfiguration.class, name = "Conditional") })
public interface AbstractConfiguration {

    String getFactorName();

    Level getLevel();

    void apply(Observation observation, SolverConfig solverConfig);

    void init(Observation observation);

    AbstractConfiguration copy();

    String toCSV();
}
