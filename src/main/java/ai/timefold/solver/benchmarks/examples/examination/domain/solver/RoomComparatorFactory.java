package ai.timefold.solver.benchmarks.examples.examination.domain.solver;

import java.util.Comparator;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.examination.domain.Examination;
import ai.timefold.solver.benchmarks.examples.examination.domain.Room;
import ai.timefold.solver.core.api.domain.common.ComparatorFactory;

public class RoomComparatorFactory implements ComparatorFactory<Examination, Room> {

    @Override
    public Comparator<Room> createComparator(Examination examination) {
        return Comparator.comparingInt(Room::getCapacity)
                .thenComparingLong(AbstractPersistable::getId);
    }

}
