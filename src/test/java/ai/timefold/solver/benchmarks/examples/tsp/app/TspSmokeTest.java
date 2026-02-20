package ai.timefold.solver.benchmarks.examples.tsp.app;

import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.examples.common.app.SolverSmokeTest;
import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;
import ai.timefold.solver.core.api.score.SimpleScore;

class TspSmokeTest extends SolverSmokeTest<TspSolution, SimpleScore> {

    private static final String UNSOLVED_DATA_FILE = "data/tsp/unsolved/europe40.json";

    @Override
    protected TspApp createCommonApp() {
        return new TspApp();
    }

    @Override
    protected Stream<TestData<SimpleScore>> testData() {
        return Stream.of(
                TestData.of(UNSOLVED_DATA_FILE,
                        SimpleScore.of(-217365),
                        SimpleScore.of(-217365)));
    }
}
