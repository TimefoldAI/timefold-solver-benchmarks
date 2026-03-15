
package ai.timefold.solver.benchmarks.examples.conferencescheduling.domain;

import java.util.SequencedSet;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;

@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class Speaker extends AbstractPersistable {

    private String name;

    private SequencedSet<Timeslot> unavailableTimeslotSet;
    private SequencedSet<String> preferredTimeslotTagSet;
    private SequencedSet<String> undesiredTimeslotTagSet;
    private SequencedSet<String> requiredRoomTagSet;
    private SequencedSet<String> preferredRoomTagSet;
    private SequencedSet<String> prohibitedRoomTagSet;
    private SequencedSet<String> undesiredRoomTagSet;

    public Speaker() {
    }

    public Speaker(long id) {
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

    public SequencedSet<Timeslot> getUnavailableTimeslotSet() {
        return unavailableTimeslotSet;
    }

    public void setUnavailableTimeslotSet(SequencedSet<Timeslot> unavailableTimeslotSet) {
        this.unavailableTimeslotSet = unavailableTimeslotSet;
    }

    public SequencedSet<String> getPreferredTimeslotTagSet() {
        return preferredTimeslotTagSet;
    }

    public void setPreferredTimeslotTagSet(SequencedSet<String> preferredTimeslotTagSet) {
        this.preferredTimeslotTagSet = preferredTimeslotTagSet;
    }

    public SequencedSet<String> getUndesiredTimeslotTagSet() {
        return undesiredTimeslotTagSet;
    }

    public void setUndesiredTimeslotTagSet(SequencedSet<String> undesiredTimeslotTagSet) {
        this.undesiredTimeslotTagSet = undesiredTimeslotTagSet;
    }

    public SequencedSet<String> getRequiredRoomTagSet() {
        return requiredRoomTagSet;
    }

    public void setRequiredRoomTagSet(SequencedSet<String> requiredRoomTagSet) {
        this.requiredRoomTagSet = requiredRoomTagSet;
    }

    public SequencedSet<String> getPreferredRoomTagSet() {
        return preferredRoomTagSet;
    }

    public void setPreferredRoomTagSet(SequencedSet<String> preferredRoomTagSet) {
        this.preferredRoomTagSet = preferredRoomTagSet;
    }

    public SequencedSet<String> getProhibitedRoomTagSet() {
        return prohibitedRoomTagSet;
    }

    public void setProhibitedRoomTagSet(SequencedSet<String> prohibitedRoomTagSet) {
        this.prohibitedRoomTagSet = prohibitedRoomTagSet;
    }

    public SequencedSet<String> getUndesiredRoomTagSet() {
        return undesiredRoomTagSet;
    }

    public void setUndesiredRoomTagSet(SequencedSet<String> undesiredRoomTagSet) {
        this.undesiredRoomTagSet = undesiredRoomTagSet;
    }

    // ************************************************************************
    // With methods
    // ************************************************************************

    public Speaker withUnavailableTimeslotSet(SequencedSet<Timeslot> unavailableTimeslotTest) {
        this.unavailableTimeslotSet = unavailableTimeslotTest;
        return this;
    }

    public Speaker withPreferredTimeslotTagSet(SequencedSet<String> preferredTimeslotTagSet) {
        this.preferredTimeslotTagSet = preferredTimeslotTagSet;
        return this;
    }

    public Speaker withUndesiredTimeslotTagSet(SequencedSet<String> undesiredTimeslotTagSet) {
        this.undesiredTimeslotTagSet = undesiredTimeslotTagSet;
        return this;
    }

    public Speaker withRequiredRoomTagSet(SequencedSet<String> requiredRoomTagSet) {
        this.requiredRoomTagSet = requiredRoomTagSet;
        return this;
    }

    public Speaker withPreferredRoomTagSet(SequencedSet<String> preferredRoomTagSet) {
        this.preferredRoomTagSet = preferredRoomTagSet;
        return this;
    }

    public Speaker withUndesiredRoomTagSet(SequencedSet<String> undesiredRoomTagSet) {
        this.undesiredRoomTagSet = undesiredRoomTagSet;
        return this;
    }

    public Speaker withProhibitedRoomTagSet(SequencedSet<String> prohibitedRoomTagSet) {
        this.prohibitedRoomTagSet = prohibitedRoomTagSet;
        return this;
    }
}
