package ai.timefold.solver.benchmarks.examples.tsp.app;

import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.examples.common.app.SolverSmokeTest;
import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;
import ai.timefold.solver.core.api.score.buildin.simplelong.SimpleLongScore;

class TspSmokeTest extends SolverSmokeTest<TspSolution, SimpleLongScore> {

    private static final String UNSOLVED_DATA_FILE = "data/tsp/unsolved/europe40.json";

    @Override
    protected TspApp createCommonApp() {
        return new TspApp();
    }

    @Override
    protected Stream<TestData<SimpleLongScore>> testData() {
        return Stream.of(
                TestData.of(UNSOLVED_DATA_FILE,
                        SimpleLongScore.of(-217365),
                        SimpleLongScore.of(-217365)));
    }
}
