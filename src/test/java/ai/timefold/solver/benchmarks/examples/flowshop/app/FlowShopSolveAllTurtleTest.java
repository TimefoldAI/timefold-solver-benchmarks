package ai.timefold.solver.benchmarks.examples.flowshop.app;

import ai.timefold.solver.benchmarks.examples.common.TestSystemProperties;
import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.app.UnsolvedDirSolveAllTurtleTest;
import ai.timefold.solver.benchmarks.examples.flowshop.domain.JobScheduleSolution;
import ai.timefold.solver.benchmarks.examples.flowshop.score.FlowShopEasyScoreCalculator;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = TestSystemProperties.TURTLE_TEST_SELECTION, matches = "flowshop|all")
class FlowShopSolveAllTurtleTest extends UnsolvedDirSolveAllTurtleTest<JobScheduleSolution> {

    @Override
    protected CommonApp<JobScheduleSolution> createCommonApp() {
        return new FlowShopApp();
    }

    @Override
    protected Class<FlowShopEasyScoreCalculator> overwritingEasyScoreCalculatorClass() {
        return FlowShopEasyScoreCalculator.class;
    }
}
