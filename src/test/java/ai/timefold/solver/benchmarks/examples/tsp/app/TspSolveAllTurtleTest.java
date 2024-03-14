package ai.timefold.solver.benchmarks.examples.tsp.app;

import ai.timefold.solver.benchmarks.examples.common.TestSystemProperties;
import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.app.UnsolvedDirSolveAllTurtleTest;
import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;
import ai.timefold.solver.benchmarks.examples.tsp.optional.score.TspEasyScoreCalculator;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = TestSystemProperties.TURTLE_TEST_SELECTION, matches = "tsp|all")
class TspSolveAllTurtleTest extends UnsolvedDirSolveAllTurtleTest<TspSolution> {

    @Override
    protected CommonApp<TspSolution> createCommonApp() {
        return new TspApp();
    }

    @Override
    protected Class<TspEasyScoreCalculator> overwritingEasyScoreCalculatorClass() {
        return TspEasyScoreCalculator.class;
    }
}
