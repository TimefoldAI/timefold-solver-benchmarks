package ai.timefold.solver.benchmarks.micro.factorial;

import java.util.List;

import ai.timefold.solver.benchmarks.micro.factorial.configuration.AbstractConfiguration;
import ai.timefold.solver.benchmarks.micro.factorial.planning.Factor;

public class FactorialConfiguration {

    private List<String> outputColumns;
    private List<Factor> factors;
    private List<AbstractConfiguration> globalConfigurations;
    private Long experimentSeed;
    private Integer completeReplications;
    private Long warmupTimeInSeconds;
    private Double warmupRatio;
    private Long observationTimeInSeconds;

    public List<String> getOutputColumns() {
        return outputColumns;
    }

    public void setOutputColumns(List<String> outputColumns) {
        this.outputColumns = outputColumns;
    }

    public List<Factor> getFactors() {
        return factors;
    }

    public void setFactors(List<Factor> factors) {
        this.factors = factors;
    }

    public List<AbstractConfiguration> getGlobalConfigurations() {
        return globalConfigurations;
    }

    public void setGlobalConfigurations(List<AbstractConfiguration> globalConfigurations) {
        this.globalConfigurations = globalConfigurations;
    }

    public Long getExperimentSeed() {
        return experimentSeed;
    }

    public void setExperimentSeed(Long experimentSeed) {
        this.experimentSeed = experimentSeed;
    }

    public Integer getCompleteReplications() {
        return completeReplications;
    }

    public void setCompleteReplications(Integer completeReplications) {
        this.completeReplications = completeReplications;
    }

    public Long getWarmupTimeInSeconds() {
        return warmupTimeInSeconds;
    }

    public void setWarmupTimeInSeconds(Long warmupTimeInSeconds) {
        this.warmupTimeInSeconds = warmupTimeInSeconds;
    }

    public Double getWarmupRatio() {
        return warmupRatio;
    }

    public void setWarmupRatio(Double warmupRatio) {
        this.warmupRatio = warmupRatio;
    }

    public Long getObservationTimeInSeconds() {
        return observationTimeInSeconds;
    }

    public void setObservationTimeInSeconds(Long observationTimeInSeconds) {
        this.observationTimeInSeconds = observationTimeInSeconds;
    }

    public void validate() {
        if (outputColumns == null || outputColumns.isEmpty()) {
            throw new IllegalArgumentException("Output columns cannot be empty");
        }
        if (factors == null || factors.isEmpty()) {
            throw new IllegalArgumentException("Factors cannot be empty");
        }
        if (completeReplications == null || completeReplications < 0) {
            throw new IllegalArgumentException("CompleteReplications cannot be empty or negative");
        }
        if (warmupTimeInSeconds == null || warmupTimeInSeconds < 0) {
            throw new IllegalArgumentException("WarmupTimeInSeconds cannot be empty or negative");
        }
        if (warmupRatio == null || warmupRatio < 0 || warmupRatio > 1) {
            throw new IllegalArgumentException("WarmupRatio must be between 0 and 1");
        }
        if (observationTimeInSeconds == null || observationTimeInSeconds < 0) {
            throw new IllegalArgumentException("ObservationTimeInSeconds cannot be empty or negative");
        }
    }
}
