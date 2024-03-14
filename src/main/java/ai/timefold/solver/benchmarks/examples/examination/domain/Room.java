package ai.timefold.solver.benchmarks.examples.examination.domain;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;

@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class Room extends AbstractPersistable {

    private int capacity;
    private int penalty;

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getPenalty() {
        return penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    @Override
    public String toString() {
        return Long.toString(id);
    }

    // ************************************************************************
    // With methods
    // ************************************************************************

    public Room withId(long id) {
        this.setId(id);
        return this;
    }

    public Room withCapacity(int capacity) {
        this.setCapacity(capacity);
        return this;
    }

    public Room withPenalty(int penalty) {
        this.setPenalty(penalty);
        return this;
    }

}
