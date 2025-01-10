package ai.timefold.solver.benchmarks.examples.meetingscheduling.domain;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RequiredAttendance.class, name = "required"),
        @JsonSubTypes.Type(value = PreferredAttendance.class, name = "preferred"),
})
@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public abstract class Attendance extends AbstractPersistable {

    private Person person;
    private Meeting meeting;

    protected Attendance() {
    }

    protected Attendance(long id, Meeting meeting) {
        super(id);
        this.meeting = meeting;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    @Override
    public String toString() {
        return person + "-" + meeting;
    }

}
