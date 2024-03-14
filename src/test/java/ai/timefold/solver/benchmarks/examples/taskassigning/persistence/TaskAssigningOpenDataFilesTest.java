package ai.timefold.solver.benchmarks.examples.taskassigning.persistence;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.OpenDataFilesTest;
import ai.timefold.solver.benchmarks.examples.taskassigning.app.TaskAssigningApp;
import ai.timefold.solver.benchmarks.examples.taskassigning.domain.TaskAssigningSolution;

class TaskAssigningOpenDataFilesTest extends OpenDataFilesTest<TaskAssigningSolution> {

    @Override
    protected CommonApp<TaskAssigningSolution> createCommonApp() {
        return new TaskAssigningApp();
    }
}
