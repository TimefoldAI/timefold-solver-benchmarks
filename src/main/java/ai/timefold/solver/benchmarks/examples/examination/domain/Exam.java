package ai.timefold.solver.benchmarks.examples.examination.domain;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;
import ai.timefold.solver.benchmarks.examples.examination.domain.solver.ExamDifficultyWeightFactory;
import ai.timefold.solver.benchmarks.examples.examination.domain.solver.RoomStrengthWeightFactory;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@PlanningEntity(difficultyWeightFactoryClass = ExamDifficultyWeightFactory.class)
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

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    @PlanningVariable(strengthWeightFactoryClass = RoomStrengthWeightFactory.class)
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

    @Override
    public String toString() {
        return topic.toString();
    }

}
