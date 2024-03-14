package ai.timefold.solver.benchmarks.examples.cloudbalancing.score;

import ai.timefold.solver.benchmarks.examples.cloudbalancing.domain.CloudBalance;
import ai.timefold.solver.benchmarks.examples.cloudbalancing.domain.CloudComputer;
import ai.timefold.solver.benchmarks.examples.cloudbalancing.domain.CloudProcess;
import ai.timefold.solver.benchmarks.examples.common.score.AbstractConstraintProviderTest;
import ai.timefold.solver.benchmarks.examples.common.score.ConstraintProviderTest;
import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

class CloudBalancingConstraintProviderTest
        extends
        AbstractConstraintProviderTest<ai.timefold.solver.benchmarks.examples.cloudbalancing.score.CloudBalancingConstraintProvider, CloudBalance> {

    @ConstraintProviderTest
    void requiredCpuPowerTotal(
            ConstraintVerifier<ai.timefold.solver.benchmarks.examples.cloudbalancing.score.CloudBalancingConstraintProvider, CloudBalance> constraintVerifier) {
        CloudComputer computer1 = new CloudComputer(1, 1, 1, 1, 2);
        CloudComputer computer2 = new CloudComputer(2, 2, 2, 2, 4);
        CloudProcess unassignedProcess = new CloudProcess(0, 1, 1, 1);
        // Total = 2, available = 1.
        CloudProcess process1 = new CloudProcess(1, 1, 1, 1);
        process1.setComputer(computer1);
        CloudProcess process2 = new CloudProcess(2, 1, 1, 1);
        process2.setComputer(computer1);
        // Total = 1, available = 2.
        CloudProcess process3 = new CloudProcess(3, 1, 1, 1);
        process3.setComputer(computer2);

        constraintVerifier.verifyThat(
                ai.timefold.solver.benchmarks.examples.cloudbalancing.score.CloudBalancingConstraintProvider::requiredCpuPowerTotal)
                .given(unassignedProcess, process1, process2, process3)
                .penalizesBy(1); // Only the first computer.
    }

    @ConstraintProviderTest
    void requiredMemoryTotal(
            ConstraintVerifier<ai.timefold.solver.benchmarks.examples.cloudbalancing.score.CloudBalancingConstraintProvider, CloudBalance> constraintVerifier) {
        CloudComputer computer1 = new CloudComputer(1, 1, 1, 1, 2);
        CloudComputer computer2 = new CloudComputer(2, 2, 2, 2, 4);
        CloudProcess unassignedProcess = new CloudProcess(0, 1, 1, 1);
        // Total = 2, available = 1.
        CloudProcess process1 = new CloudProcess(1, 1, 1, 1);
        process1.setComputer(computer1);
        CloudProcess process2 = new CloudProcess(2, 1, 1, 1);
        process2.setComputer(computer1);
        // Total = 1, available = 2.
        CloudProcess process3 = new CloudProcess(3, 1, 1, 1);
        process3.setComputer(computer2);

        constraintVerifier.verifyThat(
                ai.timefold.solver.benchmarks.examples.cloudbalancing.score.CloudBalancingConstraintProvider::requiredMemoryTotal)
                .given(unassignedProcess, process1, process2, process3)
                .penalizesBy(1); // Only the first computer.
    }

    @ConstraintProviderTest
    void requiredNetworkBandwidthTotal(
            ConstraintVerifier<ai.timefold.solver.benchmarks.examples.cloudbalancing.score.CloudBalancingConstraintProvider, CloudBalance> constraintVerifier) {
        CloudComputer computer1 = new CloudComputer(1, 1, 1, 1, 2);
        CloudComputer computer2 = new CloudComputer(2, 2, 2, 2, 4);
        CloudProcess unassignedProcess = new CloudProcess(0, 1, 1, 1);
        // Total = 2, available = 1.
        CloudProcess process1 = new CloudProcess(1, 1, 1, 1);
        process1.setComputer(computer1);
        CloudProcess process2 = new CloudProcess(2, 1, 1, 1);
        process2.setComputer(computer1);
        // Total = 1, available = 2.
        CloudProcess process3 = new CloudProcess(3, 1, 1, 1);
        process3.setComputer(computer2);

        constraintVerifier.verifyThat(
                ai.timefold.solver.benchmarks.examples.cloudbalancing.score.CloudBalancingConstraintProvider::requiredNetworkBandwidthTotal)
                .given(unassignedProcess, process1, process2, process3)
                .penalizesBy(1); // Only the first computer.
    }

    @ConstraintProviderTest
    void computerCost(
            ConstraintVerifier<ai.timefold.solver.benchmarks.examples.cloudbalancing.score.CloudBalancingConstraintProvider, CloudBalance> constraintVerifier) {
        CloudComputer computer1 = new CloudComputer(1, 1, 1, 1, 2);
        CloudComputer computer2 = new CloudComputer(2, 2, 2, 2, 4);
        CloudProcess unassignedProcess = new CloudProcess(0, 1, 1, 1);
        CloudProcess process = new CloudProcess(1, 1, 1, 1);
        process.setComputer(computer1);

        constraintVerifier.verifyThat(
                ai.timefold.solver.benchmarks.examples.cloudbalancing.score.CloudBalancingConstraintProvider::computerCost)
                .given(computer1, computer2, unassignedProcess, process)
                .penalizesBy(2);
    }

    @Override
    protected
            ConstraintVerifier<ai.timefold.solver.benchmarks.examples.cloudbalancing.score.CloudBalancingConstraintProvider, CloudBalance>
            createConstraintVerifier() {
        return ConstraintVerifier.build(new CloudBalancingConstraintProvider(), CloudBalance.class, CloudProcess.class);
    }

}
