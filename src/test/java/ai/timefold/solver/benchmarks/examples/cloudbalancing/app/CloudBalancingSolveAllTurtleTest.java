package ai.timefold.solver.benchmarks.examples.cloudbalancing.app;

import ai.timefold.solver.benchmarks.examples.cloudbalancing.domain.CloudBalance;
import ai.timefold.solver.benchmarks.examples.cloudbalancing.optional.score.CloudBalancingMapBasedEasyScoreCalculator;
import ai.timefold.solver.benchmarks.examples.common.TestSystemProperties;
import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.app.UnsolvedDirSolveAllTurtleTest;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = TestSystemProperties.TURTLE_TEST_SELECTION, matches = "cloudbalancing|all")
class CloudBalancingSolveAllTurtleTest extends UnsolvedDirSolveAllTurtleTest<CloudBalance> {

    @Override
    protected CommonApp<CloudBalance> createCommonApp() {
        return new CloudBalancingApp();
    }

    @Override
    protected Class<CloudBalancingMapBasedEasyScoreCalculator> overwritingEasyScoreCalculatorClass() {
        return CloudBalancingMapBasedEasyScoreCalculator.class;
    }
}
