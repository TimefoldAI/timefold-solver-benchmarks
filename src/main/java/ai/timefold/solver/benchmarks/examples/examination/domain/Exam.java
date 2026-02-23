package ai.timefold.solver.benchmarks.examples.examination.domain;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;
import ai.timefold.solver.benchmarks.examples.examination.domain.solver.ExamComparatorFactory;
import ai.timefold.solver.benchmarks.examples.examination.domain.solver.RoomComparatorFactory;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@PlanningEntity(comparatorFactoryClass = ExamComparatorFactory.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LeadingExam.class, name = "leading"),
        @JsonSubTypes.Type(value = FollowingExam.class, name = "following"),
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public abstract class Exam extends AbstractPersistable {

    protected Topic topic;

    // Planning variables: changes during planning, between score calculations.
    protected Room room;

    private int studentSizeTotal = -1;
    private int maximumDuration = -1;

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    @PlanningVariable(comparatorFactoryClass = RoomComparatorFactory.class)
    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public abstract Period getPeriod();

    @JsonIgnore
    public int getTopicDuration() {
        return getTopic().getDuration();
    }

    @JsonIgnore
    public int getTopicStudentSize() {
        return getTopic().getStudentSize();
    }

    @JsonIgnore
    public int getDayIndex() {
        Period period = getPeriod();
        if (period == null) {
            return Integer.MIN_VALUE;
        }
        return period.getDayIndex();
    }

    @JsonIgnore
    public int getPeriodIndex() {
        Period period = getPeriod();
        if (period == null) {
            return Integer.MIN_VALUE;
        }
        return period.getPeriodIndex();
    }

    @JsonIgnore
    public int getPeriodDuration() {
        Period period = getPeriod();
        if (period == null) {
            return Integer.MIN_VALUE;
        }
        return period.getDuration();
    }

    @JsonIgnore
    public boolean isTopicFrontLoadLarge() {
        return topic.isFrontLoadLarge();
    }

    @JsonIgnore
    public boolean isPeriodFrontLoadLast() {
        Period period = getPeriod();
        if (period == null) {
            return false;
        }
        return period.isFrontLoadLast();
    }

    @JsonIgnore
    public int getStudentSizeTotal(Examination examination) {
        computeInformation(examination);
        return studentSizeTotal;
    }

    @JsonIgnore
    public int getMaximumDuration(Examination examination) {
        computeInformation(examination);
        return maximumDuration;
    }

    @JsonIgnore
    private void computeInformation(Examination examination) {
        if (studentSizeTotal != -1 && maximumDuration != -1) {
            return;
        }
        studentSizeTotal = getTopicStudentSize();
        maximumDuration = getTopicDuration();
        for (var periodPenalty : examination.getPeriodPenaltyList()) {
            if (periodPenalty.getLeftTopic().equals(getTopic())) {
                switch (periodPenalty.getPeriodPenaltyType()) {
                    case EXAM_COINCIDENCE:
                        studentSizeTotal += periodPenalty.getRightTopic().getStudentSize();
                        maximumDuration = Math.max(maximumDuration, periodPenalty.getRightTopic().getDuration());
                        break;
                    case EXCLUSION, AFTER:
                        // Do nothing
                        break;
                    default:
                        throw new IllegalStateException("The periodPenaltyType (%s) is not implemented."
                                .formatted(periodPenalty.getPeriodPenaltyType()));
                }
            } else if (periodPenalty.getRightTopic().equals(getTopic())) {
                switch (periodPenalty.getPeriodPenaltyType()) {
                    case EXAM_COINCIDENCE, AFTER:
                        studentSizeTotal += periodPenalty.getLeftTopic().getStudentSize();
                        maximumDuration = Math.max(maximumDuration, periodPenalty.getLeftTopic().getDuration());
                        break;
                    case EXCLUSION:
                        // Do nothing
                        break;
                    default:
                        throw new IllegalStateException("The periodPenaltyType (%s) is not implemented."
                                .formatted(periodPenalty.getPeriodPenaltyType()));
                }
            }
        }
    }

    @Override
    public String toString() {
        return topic.toString();
    }

}
