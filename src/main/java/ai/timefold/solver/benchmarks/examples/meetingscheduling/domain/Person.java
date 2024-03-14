package ai.timefold.solver.benchmarks.examples.meetingscheduling.domain;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;

public class Person extends AbstractPersistable {

    private String fullName;

    public Person() {
    }

    public Person(long id, String fullName) {
        super(id);
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return fullName;
    }

}
