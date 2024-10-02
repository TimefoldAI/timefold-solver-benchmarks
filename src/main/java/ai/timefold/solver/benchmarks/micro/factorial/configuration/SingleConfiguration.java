package ai.timefold.solver.benchmarks.micro.factorial.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.localsearch.decider.acceptor.AcceptorType;
import ai.timefold.solver.core.config.localsearch.decider.acceptor.LocalSearchAcceptorConfig;
import ai.timefold.solver.core.config.localsearch.decider.acceptor.ReconfigurationConfig;
import ai.timefold.solver.core.config.localsearch.decider.forager.LocalSearchForagerConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;

public class SingleConfiguration extends AbstractConfiguration {
    private final String key;
    private final Object value;

    public SingleConfiguration(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String toCSV() {
        return value != null ? value.toString() : "-";
    }

    @Override
    public void apply(SolverConfig solverConfig) {
        switch (key) {
            case "runTimeInSeconds": {
                solverConfig.withTerminationConfig(new TerminationConfig().withSecondsSpentLimit((Long) value));
                break;
            }
            case "runTimeInMinutes": {
                solverConfig.withTerminationConfig(new TerminationConfig().withMinutesSpentLimit((Long) value));
                break;
            }
            case "seed": {
                solverConfig.withRandomSeed((Long) value);
                break;
            }
            case "moveCountLimitPercentage": {
                var acceptorConfig = getAcceptorConfig(solverConfig);
                var reconfigurationConfig = Objects.requireNonNullElse(acceptorConfig.getReconfigurationConfig(),
                        new ReconfigurationConfig());
                reconfigurationConfig.withMoveCountLimitPercentage((Double) value);
                acceptorConfig.withReconfiguration(reconfigurationConfig);
                break;
            }
            case "lateAcceptanceReconfigurationSize": {
                var acceptorConfig = getAcceptorConfig(solverConfig);
                var reconfigurationConfig = Objects.requireNonNullElse(acceptorConfig.getReconfigurationConfig(),
                        new ReconfigurationConfig());
                reconfigurationConfig.withReconfigurationRatio((Long) value);
                acceptorConfig.withReconfiguration(reconfigurationConfig);
                break;
            }
            case "selectedCountLimitRatio": {
                var foragerConfig = getForagerConfig(solverConfig);
                foragerConfig.withSelectedCountLimitRatio((Double) value);
                break;
            }
            default:
                // ignore unknown properties
                break;
        }
    }

    private LocalSearchPhaseConfig getLocalSearchConfig(SolverConfig solverConfig) {
        var phaseConfigList = solverConfig.getPhaseConfigList();
        if (phaseConfigList == null) {
            phaseConfigList = new ArrayList<>();
            phaseConfigList.add(new ConstructionHeuristicPhaseConfig());
            phaseConfigList.add(new LocalSearchPhaseConfig());
            solverConfig.withPhaseList(phaseConfigList);
        }
        return (LocalSearchPhaseConfig) phaseConfigList.stream().filter(LocalSearchPhaseConfig.class::isInstance)
                .findFirst()
                .get();
    }

    private LocalSearchAcceptorConfig getAcceptorConfig(SolverConfig solverConfig) {
        var localSearchConfig = getLocalSearchConfig(solverConfig);
        var acceptorConfig = Objects.requireNonNullElse(localSearchConfig.getAcceptorConfig(),
                new LocalSearchAcceptorConfig());
        if (acceptorConfig.getAcceptorTypeList() == null) {
            acceptorConfig.withAcceptorTypeList(List.of(AcceptorType.LATE_ACCEPTANCE));
        }
        localSearchConfig.withAcceptorConfig(acceptorConfig);
        return acceptorConfig;
    }

    private LocalSearchForagerConfig getForagerConfig(SolverConfig solverConfig) {
        var localSearchConfig = getLocalSearchConfig(solverConfig);
        var foragerConfig = Objects.requireNonNullElse(localSearchConfig.getForagerConfig(),
                new LocalSearchForagerConfig());
        if (foragerConfig.getAcceptedCountLimit() == null) {
            foragerConfig.withAcceptedCountLimit(1);
        }
        localSearchConfig.withForagerConfig(foragerConfig);
        return foragerConfig;
    }

    @Override
    public String toString() {
        return "SingleConfiguration{" +
                "key='" + key + '\'' +
                ", value=" + value +
                '}';
    }
}
