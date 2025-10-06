package ai.timefold.solver.benchmarks.examples.flowshop.app;

import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.examples.common.app.SolverSmokeTest;
import ai.timefold.solver.benchmarks.examples.flowshop.domain.JobScheduleSolution;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

class FlowShopSmokeTest extends SolverSmokeTest<JobScheduleSolution, HardSoftLongScore> {

    private static final String UNSOLVED_DATA_FILE = "data/flowshop/unsolved/Ta001.json";

    @Override
    protected FlowShopApp createCommonApp() {
        return new FlowShopApp();
    }

    @Override
    protected Stream<TestData<HardSoftLongScore>> testData() {
        return Stream.of(
                TestData.of(UNSOLVED_DATA_FILE,
                        HardSoftLongScore.of(0, -1278),
                        HardSoftLongScore.of(0, -1278)));
    }
}
