package ai.timefold.solver.benchmarks.examples.cloudbalancing.score;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.sum;
import static ai.timefold.solver.core.api.score.stream.Joiners.equal;

import java.util.function.Function;

import ai.timefold.solver.benchmarks.examples.cloudbalancing.domain.CloudComputer;
import ai.timefold.solver.benchmarks.examples.cloudbalancing.domain.CloudProcess;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

public class CloudBalancingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                requiredCpuPowerTotal(constraintFactory),
                requiredMemoryTotal(constraintFactory),
                requiredNetworkBandwidthTotal(constraintFactory),
                computerCost(constraintFactory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    Constraint requiredCpuPowerTotal(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(CloudProcess.class)
                .groupBy(CloudProcess::getComputer, sum(CloudProcess::getRequiredCpuPower))
                .filter((computer, requiredCpuPower) -> requiredCpuPower > computer.getCpuPower())
                .penalize(HardSoftScore.ONE_HARD,
                        (computer, requiredCpuPower) -> requiredCpuPower - computer.getCpuPower())
                .asConstraint("requiredCpuPowerTotal");
    }

    Constraint requiredMemoryTotal(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(CloudProcess.class)
                .groupBy(CloudProcess::getComputer, sum(CloudProcess::getRequiredMemory))
                .filter((computer, requiredMemory) -> requiredMemory > computer.getMemory())
                .penalize(HardSoftScore.ONE_HARD,
                        (computer, requiredMemory) -> requiredMemory - computer.getMemory())
                .asConstraint("requiredMemoryTotal");
    }

    Constraint requiredNetworkBandwidthTotal(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(CloudProcess.class)
                .groupBy(CloudProcess::getComputer, sum(CloudProcess::getRequiredNetworkBandwidth))
                .filter((computer, requiredNetworkBandwidth) -> requiredNetworkBandwidth > computer.getNetworkBandwidth())
                .penalize(HardSoftScore.ONE_HARD,
                        (computer, requiredNetworkBandwidth) -> requiredNetworkBandwidth - computer.getNetworkBandwidth())
                .asConstraint("requiredNetworkBandwidthTotal");
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    Constraint computerCost(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(CloudComputer.class)
                .ifExists(CloudProcess.class, equal(Function.identity(), CloudProcess::getComputer))
                .penalize(HardSoftScore.ONE_SOFT, CloudComputer::getCost)
                .asConstraint("computerCost");
    }

}
