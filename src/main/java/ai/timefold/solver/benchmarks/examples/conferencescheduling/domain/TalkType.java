package ai.timefold.solver.benchmarks.examples.conferencescheduling.domain;

import java.util.LinkedHashSet;
import java.util.SequencedSet;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;

@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class TalkType extends AbstractPersistable {

    private String name;

    private SequencedSet<Timeslot> compatibleTimeslotSet;
    private SequencedSet<Room> compatibleRoomSet;

    public TalkType() {
    }

    public TalkType(long id) {
        super(id);
    }

    public TalkType(long id, String name) {
        super(id);
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SequencedSet<Timeslot> getCompatibleTimeslotSet() {
        return compatibleTimeslotSet;
    }

    public void setCompatibleTimeslotSet(SequencedSet<Timeslot> compatibleTimeslotSet) {
        this.compatibleTimeslotSet = new LinkedHashSet<>(compatibleTimeslotSet);
    }

    public SequencedSet<Room> getCompatibleRoomSet() {
        return compatibleRoomSet;
    }

    public void setCompatibleRoomSet(SequencedSet<Room> compatibleRoomSet) {
        this.compatibleRoomSet = new LinkedHashSet<>(compatibleRoomSet);
    }

}
