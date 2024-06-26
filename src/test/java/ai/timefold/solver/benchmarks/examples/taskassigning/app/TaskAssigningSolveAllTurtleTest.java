package ai.timefold.solver.benchmarks.examples.taskassigning.app;

import ai.timefold.solver.benchmarks.examples.common.TestSystemProperties;
import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.app.UnsolvedDirSolveAllTurtleTest;
import ai.timefold.solver.benchmarks.examples.taskassigning.domain.TaskAssigningSolution;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = TestSystemProperties.TURTLE_TEST_SELECTION, matches = "taskassigning|all")
class TaskAssigningSolveAllTurtleTest extends UnsolvedDirSolveAllTurtleTest<TaskAssigningSolution> {

    @Override
    protected CommonApp<TaskAssigningSolution> createCommonApp() {
        return new TaskAssigningApp();
    }
}
