package ai.timefold.solver.benchmarks.examples.meetingscheduling.domain;

import java.util.List;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;

@PlanningSolution
public class MeetingSchedule extends AbstractPersistable {

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides;

    @ProblemFactCollectionProperty
    private List<Meeting> meetingList;
    @ProblemFactCollectionProperty
    private List<Day> dayList;
    @ValueRangeProvider
    @ProblemFactCollectionProperty
    private List<TimeGrain> timeGrainList;
    @ValueRangeProvider
    @ProblemFactCollectionProperty
    private List<Room> roomList;
    @ProblemFactCollectionProperty
    private List<Person> personList;
    @ProblemFactCollectionProperty
    private List<Attendance> attendanceList;

    @PlanningEntityCollectionProperty
    private List<MeetingAssignment> meetingAssignmentList;

    @PlanningScore
    private HardMediumSoftScore score;

    public MeetingSchedule() {
    }

    public MeetingSchedule(long id) {
        super(id);
    }

    public ConstraintWeightOverrides<HardMediumSoftScore> getConstraintWeightOverrides() {
        return constraintWeightOverrides;
    }

    public void setConstraintWeightOverrides(ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    public List<Meeting> getMeetingList() {
        return meetingList;
    }

    public void setMeetingList(List<Meeting> meetingList) {
        this.meetingList = meetingList;
    }

    public List<Day> getDayList() {
        return dayList;
    }

    public void setDayList(List<Day> dayList) {
        this.dayList = dayList;
    }

    public List<TimeGrain> getTimeGrainList() {
        return timeGrainList;
    }

    public void setTimeGrainList(List<TimeGrain> timeGrainList) {
        this.timeGrainList = timeGrainList;
    }

    public List<Room> getRoomList() {
        return roomList;
    }

    public void setRoomList(List<Room> roomList) {
        this.roomList = roomList;
    }

    public List<Person> getPersonList() {
        return personList;
    }

    public void setPersonList(List<Person> personList) {
        this.personList = personList;
    }

    public List<Attendance> getAttendanceList() {
        return attendanceList;
    }

    public void setAttendanceList(List<Attendance> attendanceList) {
        this.attendanceList = attendanceList;
    }

    public List<MeetingAssignment> getMeetingAssignmentList() {
        return meetingAssignmentList;
    }

    public void setMeetingAssignmentList(List<MeetingAssignment> meetingAssignmentList) {
        this.meetingAssignmentList = meetingAssignmentList;
    }

    public HardMediumSoftScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
