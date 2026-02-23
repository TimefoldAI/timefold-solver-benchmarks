package ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.solver;

import java.util.Comparator;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.CourseSchedule;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.Room;
import ai.timefold.solver.core.api.domain.common.ComparatorFactory;

public class RoomComparatorFactory implements ComparatorFactory<CourseSchedule, Room> {

    @Override
    public Comparator<Room> createComparator(CourseSchedule schedule) {
        return Comparator.comparingInt(Room::getCapacity)
                .thenComparingLong(AbstractPersistable::getId);
    }
}
