package ai.timefold.solver.benchmarks.examples.conferencescheduling.domain;

import java.util.Set;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;

@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class Room extends AbstractPersistable {

    private String name;
    private int capacity;

    private Set<TalkType> talkTypeSet;
    private Set<Timeslot> unavailableTimeslotSet;
    private Set<String> tagSet;

    public Room() {
    }

    public Room(long id) {
        super(id);
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

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Set<TalkType> getTalkTypeSet() {
        return talkTypeSet;
    }

    public void setTalkTypeSet(Set<TalkType> talkTypeSet) {
        this.talkTypeSet = talkTypeSet;
    }

    public Set<String> getTagSet() {
        return tagSet;
    }

    public void setTagSet(Set<String> tagSet) {
        this.tagSet = tagSet;
    }

    // ************************************************************************
    // With methods
    // ************************************************************************

    public Room withCapacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    public Room withTalkTypeSet(Set<TalkType> talkTypeSet) {
        this.talkTypeSet = talkTypeSet;
        return this;
    }

    public Room withTagSet(Set<String> tagSet) {
        this.tagSet = tagSet;
        return this;
    }

}
