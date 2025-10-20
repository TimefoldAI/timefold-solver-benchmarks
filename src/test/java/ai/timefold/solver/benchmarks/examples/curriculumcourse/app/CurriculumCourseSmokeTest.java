package ai.timefold.solver.benchmarks.examples.curriculumcourse.app;

import ai.timefold.solver.benchmarks.examples.common.app.SolverSmokeTest;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.CourseSchedule;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.stream.Stream;

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
                        HardSoftScore.ofSoft(-16),
                        HardSoftScore.ofSoft(-21)));
    }
}
