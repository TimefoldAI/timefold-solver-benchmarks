package ai.timefold.solver.benchmarks.examples.flowshop.app;

import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.examples.common.app.SolverSmokeTest;
import ai.timefold.solver.benchmarks.examples.flowshop.domain.JobScheduleSolution;
import ai.timefold.solver.core.api.score.HardSoftScore;

class FlowShopSmokeTest extends SolverSmokeTest<JobScheduleSolution, HardSoftScore> {

    private static final String UNSOLVED_DATA_FILE = "data/flowshop/unsolved/Ta001.json";

    @Override
    protected FlowShopApp createCommonApp() {
        return new FlowShopApp();
    }

    @Override
    protected Stream<TestData<HardSoftScore>> testData() {
        return Stream.of(
                TestData.of(UNSOLVED_DATA_FILE,
                        HardSoftScore.of(0, -1278),
                        HardSoftScore.of(0, -1278)));
    }
}
