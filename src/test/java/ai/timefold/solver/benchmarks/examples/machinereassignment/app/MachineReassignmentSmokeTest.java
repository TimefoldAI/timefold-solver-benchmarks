package ai.timefold.solver.benchmarks.examples.machinereassignment.app;

import ai.timefold.solver.benchmarks.examples.common.app.SolverSmokeTest;
import ai.timefold.solver.benchmarks.examples.machinereassignment.domain.MachineReassignment;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

import java.util.stream.Stream;

class MachineReassignmentSmokeTest extends SolverSmokeTest<MachineReassignment, HardSoftLongScore> {

    private static final String UNSOLVED_DATA_FILE = "data/machinereassignment/unsolved/model_a2_1.json";

    @Override
    protected MachineReassignmentApp createCommonApp() {
        return new MachineReassignmentApp();
    }

    @Override
    protected Stream<TestData<HardSoftLongScore>> testData() {
        return Stream.of(
                TestData.of(UNSOLVED_DATA_FILE,
                        HardSoftLongScore.ofSoft(-7483748),
                        HardSoftLongScore.ofSoft(-10306055)));
    }
}
