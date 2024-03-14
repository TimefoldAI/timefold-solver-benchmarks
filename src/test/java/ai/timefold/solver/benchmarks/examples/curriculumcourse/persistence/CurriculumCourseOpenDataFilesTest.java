package ai.timefold.solver.benchmarks.examples.curriculumcourse.persistence;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.OpenDataFilesTest;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.app.CurriculumCourseApp;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.CourseSchedule;

class CurriculumCourseOpenDataFilesTest extends OpenDataFilesTest<CourseSchedule> {

    @Override
    protected CommonApp<CourseSchedule> createCommonApp() {
        return new CurriculumCourseApp();
    }
}
