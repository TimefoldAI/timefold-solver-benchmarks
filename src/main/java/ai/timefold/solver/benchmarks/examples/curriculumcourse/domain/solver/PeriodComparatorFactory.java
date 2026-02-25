package ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.solver;

import java.util.Comparator;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.CourseSchedule;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.Period;
import ai.timefold.solver.core.api.domain.common.ComparatorFactory;

public class PeriodComparatorFactory implements ComparatorFactory<CourseSchedule, Period> {

    @Override
    public Comparator<Period> createComparator(CourseSchedule schedule) {
        return Comparator
                .<Period, Integer> comparing(period -> period.getUnavailablePeriodPenaltyCount(schedule),
                        Comparator.reverseOrder())
                .thenComparingInt(period -> period.getDay().getDayIndex())
                .thenComparingInt(period -> period.getTimeslot().getTimeslotIndex())
                .thenComparingLong(AbstractPersistable::getId);
    }
}
