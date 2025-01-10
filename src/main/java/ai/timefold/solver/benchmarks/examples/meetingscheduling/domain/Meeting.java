package ai.timefold.solver.benchmarks.examples.meetingscheduling.domain;

import java.util.List;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class Meeting extends AbstractPersistable {

    private String topic;
    /**
     * Multiply by {@link TimeGrain#GRAIN_LENGTH_IN_MINUTES} to get duration in minutes.
     */
    private int durationInGrains;

    private List<RequiredAttendance> requiredAttendanceList;
    private List<PreferredAttendance> preferredAttendanceList;

    public Meeting() {
    }

    public Meeting(long id) {
        super(id);
    }

    public Meeting(long id, String topic, int durationInGrains) {
        this(id);
        this.topic = topic;
        this.durationInGrains = durationInGrains;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getDurationInGrains() {
        return durationInGrains;
    }

    public void setDurationInGrains(int durationInGrains) {
        this.durationInGrains = durationInGrains;
    }

    public List<RequiredAttendance> getRequiredAttendanceList() {
        return requiredAttendanceList;
    }

    public void setRequiredAttendanceList(List<RequiredAttendance> requiredAttendanceList) {
        this.requiredAttendanceList = requiredAttendanceList;
    }

    public List<PreferredAttendance> getPreferredAttendanceList() {
        return preferredAttendanceList;
    }

    public void setPreferredAttendanceList(List<PreferredAttendance> preferredAttendanceList) {
        this.preferredAttendanceList = preferredAttendanceList;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @JsonIgnore
    public int getRequiredCapacity() {
        return requiredAttendanceList.size() + preferredAttendanceList.size();
    }

    @Override
    public String toString() {
        return topic;
    }
}
