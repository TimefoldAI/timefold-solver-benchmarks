package ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.solver;

import java.util.Comparator;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.CourseSchedule;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.Lecture;
import ai.timefold.solver.core.api.domain.common.ComparatorFactory;

public class LectureComparatorFactory implements ComparatorFactory<CourseSchedule, Lecture> {

    @Override
    public Comparator<Lecture> createComparator(CourseSchedule schedule) {
        return Comparator.<Lecture> comparingInt(lecture -> lecture.getCurriculumSet().size())
                .thenComparingInt(lecture -> lecture.getUnavailablePeriodPenaltyCount(schedule))
                .thenComparingInt(lecture -> lecture.getCourse().getLectureSize())
                .thenComparingInt(lecture -> lecture.getCourse().getStudentSize())
                .thenComparingInt(lecture -> lecture.getCourse().getMinWorkingDaySize())
                .thenComparingLong(AbstractPersistable::getId);
    }
}
