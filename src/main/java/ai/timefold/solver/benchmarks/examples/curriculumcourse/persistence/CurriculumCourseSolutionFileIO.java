package ai.timefold.solver.benchmarks.examples.curriculumcourse.persistence;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractJsonSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.CourseSchedule;

public class CurriculumCourseSolutionFileIO extends AbstractJsonSolutionFileIO<CourseSchedule> {

    public CurriculumCourseSolutionFileIO() {
        super(CourseSchedule.class);
    }
}
