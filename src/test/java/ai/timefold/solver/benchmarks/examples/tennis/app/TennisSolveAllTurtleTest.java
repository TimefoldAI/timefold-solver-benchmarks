package ai.timefold.solver.benchmarks.examples.tennis.app;

import ai.timefold.solver.benchmarks.examples.common.TestSystemProperties;
import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.app.UnsolvedDirSolveAllTurtleTest;
import ai.timefold.solver.benchmarks.examples.tennis.domain.TennisSolution;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = TestSystemProperties.TURTLE_TEST_SELECTION, matches = "tennis|all")
class TennisSolveAllTurtleTest extends UnsolvedDirSolveAllTurtleTest<TennisSolution> {

    @Override
    protected CommonApp<TennisSolution> createCommonApp() {
        return new TennisApp();
    }
}
