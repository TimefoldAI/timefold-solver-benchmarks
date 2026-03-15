package ai.timefold.solver.benchmarks.examples.tsp.app;

import ai.timefold.solver.benchmarks.examples.common.TestSystemProperties;
import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.app.UnsolvedDirSolveAllTurtleTest;
import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = TestSystemProperties.TURTLE_TEST_SELECTION, matches = "tsp|all")
class TspSolveAllTurtleTest extends UnsolvedDirSolveAllTurtleTest<TspSolution> {

    @Override
    protected CommonApp<TspSolution> createCommonApp() {
        return new TspApp();
    }

}
