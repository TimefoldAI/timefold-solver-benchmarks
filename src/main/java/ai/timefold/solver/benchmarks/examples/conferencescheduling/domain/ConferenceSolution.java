package ai.timefold.solver.benchmarks.examples.conferencescheduling.domain;

import java.util.List;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.core.api.domain.constraintweight.ConstraintConfigurationProvider;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

@PlanningSolution
public class ConferenceSolution extends AbstractPersistable {

    private String conferenceName;
    @ConstraintConfigurationProvider
    private ConferenceConstraintConfiguration constraintConfiguration;

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
    private HardMediumSoftScore score = null;

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

    public ConferenceConstraintConfiguration getConstraintConfiguration() {
        return constraintConfiguration;
    }

    public void setConstraintConfiguration(ConferenceConstraintConfiguration constraintConfiguration) {
        this.constraintConfiguration = constraintConfiguration;
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

    public HardMediumSoftScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }

    // ************************************************************************
    // With methods
    // ************************************************************************

    public ConferenceSolution withConstraintConfiguration(ConferenceConstraintConfiguration constraintConfiguration) {
        this.constraintConfiguration = constraintConfiguration;
        return this;
    }

    public ConferenceSolution withTalkTypeList(List<TalkType> talkTypeList) {
        this.talkTypeList = talkTypeList;
        return this;
    }

    public ConferenceSolution withTimeslotList(List<Timeslot> timeslotList) {
        this.timeslotList = timeslotList;
        return this;
    }

    public ConferenceSolution withRoomList(List<Room> roomList) {
        this.roomList = roomList;
        return this;
    }

    public ConferenceSolution withSpeakerList(List<Speaker> speakerList) {
        this.speakerList = speakerList;
        return this;
    }

    public ConferenceSolution withTalkList(List<Talk> talkList) {
        this.talkList = talkList;
        return this;
    }

}
