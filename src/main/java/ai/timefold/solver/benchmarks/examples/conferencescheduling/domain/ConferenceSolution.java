package ai.timefold.solver.benchmarks.examples.conferencescheduling.domain;

import java.util.List;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;
import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.score.HardSoftScore;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;

@PlanningSolution
@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class ConferenceSolution extends AbstractPersistable {

    private String conferenceName;

    @ProblemFactProperty
    private ConferenceConstraintProperties constraintProperties;

    private ConstraintWeightOverrides<HardSoftScore> constraintWeightOverrides;

    @ProblemFactCollectionProperty
    private List<TalkType> talkTypeList;

    @ProblemFactCollectionProperty
    private List<Timeslot> timeslotList;

    @ProblemFactCollectionProperty
    private List<Room> roomList;

    @ProblemFactCollectionProperty
    private List<Speaker> speakerList;

    @PlanningEntityCollectionProperty
    private List<Talk> talkList;

    @PlanningScore
    private HardSoftScore score = null;

    public ConferenceSolution() {
    }

    public ConferenceSolution(long id) {
        super(id);
    }

    @Override
    public String toString() {
        return conferenceName;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getConferenceName() {
        return conferenceName;
    }

    public void setConferenceName(String conferenceName) {
        this.conferenceName = conferenceName;
    }

    public ConferenceConstraintProperties getConstraintProperties() {
        return constraintProperties;
    }

    public void setConstraintProperties(ConferenceConstraintProperties constraintProperties) {
        this.constraintProperties = constraintProperties;
    }

    public ConstraintWeightOverrides<HardSoftScore> getConstraintWeightOverrides() {
        return constraintWeightOverrides;
    }

    public void setConstraintWeightOverrides(ConstraintWeightOverrides<HardSoftScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    public List<TalkType> getTalkTypeList() {
        return talkTypeList;
    }

    public void setTalkTypeList(List<TalkType> talkTypeList) {
        this.talkTypeList = talkTypeList;
    }

    public List<Timeslot> getTimeslotList() {
        return timeslotList;
    }

    public void setTimeslotList(List<Timeslot> timeslotList) {
        this.timeslotList = timeslotList;
    }

    public List<Room> getRoomList() {
        return roomList;
    }

    public void setRoomList(List<Room> roomList) {
        this.roomList = roomList;
    }

    public List<Speaker> getSpeakerList() {
        return speakerList;
    }

    public void setSpeakerList(List<Speaker> speakerList) {
        this.speakerList = speakerList;
    }

    public List<Talk> getTalkList() {
        return talkList;
    }

    public void setTalkList(List<Talk> talkList) {
        this.talkList = talkList;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

}
