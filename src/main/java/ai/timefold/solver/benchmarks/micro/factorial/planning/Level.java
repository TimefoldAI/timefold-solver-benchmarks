package ai.timefold.solver.benchmarks.micro.factorial.planning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.localsearch.decider.acceptor.AcceptorType;
import ai.timefold.solver.core.config.localsearch.decider.acceptor.LocalSearchAcceptorConfig;
import ai.timefold.solver.core.config.localsearch.decider.forager.LocalSearchForagerConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;

public class Level {

    private final String value;
    protected final Factor factor;

    public Level(Factor factor, String value) {
        this.factor = Objects.requireNonNull(factor);
        this.value = Objects.requireNonNull(value);
    }

    public String getFactorName() {
        return factor.getName();
    }

    public String getValue() {
        return value;
    }

    public void apply(Observation observation, SolverConfig solverConfig) {
        switch (factor.getName()) {
            case "runTimeInSeconds": {
                solverConfig.withTerminationConfig(new TerminationConfig().withSecondsSpentLimit(Long.parseLong(value)));
                break;
            }
            case "observationSeed": {
                solverConfig.withRandomSeed(Long.parseLong(value));
                break;
            }
            case "acceptorType": {
                var acceptorConfig = getAcceptorConfig(solverConfig);
                var type = Arrays.stream(AcceptorType.values()).filter(a -> a.name().equals(value)).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unknown acceptor type: %s".formatted(value)));
                acceptorConfig.withAcceptorTypeList(List.of(type));
                break;
            }
            case "lateAcceptanceSize": {
                var acceptorConfig = getAcceptorConfig(solverConfig);
                acceptorConfig.setLateAcceptanceSize(Integer.parseInt(value));
                break;
            }
            default: //ignore
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
        return "Level{" +
                "factor=" + factor.getName() +
                ", level=" + value +
                '}';
    }
}
