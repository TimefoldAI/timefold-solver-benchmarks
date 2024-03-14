package ai.timefold.solver.benchmarks.examples.projectjobscheduling.persistence;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.OpenDataFilesTest;
import ai.timefold.solver.benchmarks.examples.projectjobscheduling.app.ProjectJobSchedulingApp;
import ai.timefold.solver.benchmarks.examples.projectjobscheduling.domain.Schedule;

class ProjectJobSchedulingOpenDataFilesTest extends OpenDataFilesTest<Schedule> {

    @Override
    protected CommonApp<Schedule> createCommonApp() {
        return new ProjectJobSchedulingApp();
    }
}
