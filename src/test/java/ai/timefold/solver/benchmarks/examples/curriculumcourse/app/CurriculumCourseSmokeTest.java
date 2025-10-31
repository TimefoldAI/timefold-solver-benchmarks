package ai.timefold.solver.benchmarks.examples.curriculumcourse.app;

import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.examples.common.app.SolverSmokeTest;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.CourseSchedule;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;

class CurriculumCourseSmokeTest extends SolverSmokeTest<CourseSchedule, HardSoftScore> {

    private static final String UNSOLVED_DATA_FILE = "data/curriculumcourse/unsolved/comp01_initialized.json";

    @Override
    protected CurriculumCourseApp createCommonApp() {
        return new CurriculumCourseApp();
    }

    @Override
    protected Stream<TestData<HardSoftScore>> testData() {
        return Stream.of(
                TestData.of(UNSOLVED_DATA_FILE,
                        HardSoftScore.ofSoft(-20),
                        HardSoftScore.ofSoft(-21)));
    }
}
