package ai.timefold.solver.benchmarks.examples.curriculumcourse.domain;

import java.util.Set;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.solver.LectureComparatorFactory;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.solver.PeriodComparatorFactory;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.solver.RoomComparatorFactory;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity(comparatorFactoryClass = LectureComparatorFactory.class)
public class Lecture extends AbstractPersistable {

    private Course course;
    private int lectureIndexInCourse;
    private int unavailablePeriodPenaltyCount = -1;
    private boolean pinned;

    // Planning variables: changes during planning, between score calculations.
    private Period period;
    private Room room;

    public Lecture() {
    }

    public Lecture(long id, Course course,
            int lectureIndexInCourse, boolean pinned) {
        super(id);
        this.course = course;
        this.lectureIndexInCourse = lectureIndexInCourse;
        this.pinned = pinned;
    }

    public Lecture(long id, Course course,
            Period period, Room room) {
        super(id);
        this.course = course;
        this.period = period;
        this.room = room;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public int getLectureIndexInCourse() {
        return lectureIndexInCourse;
    }

    public void setLectureIndexInCourse(int lectureIndexInCourse) {
        this.lectureIndexInCourse = lectureIndexInCourse;
    }

    @PlanningPin
    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    @PlanningVariable(comparatorFactoryClass = PeriodComparatorFactory.class)
    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
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

    @JsonIgnore
    public Teacher getTeacher() {
        return course.getTeacher();
    }

    @JsonIgnore
    public int getStudentSize() {
        return course.getStudentSize();
    }

    @JsonIgnore
    public Set<Curriculum> getCurriculumSet() {
        return course.getCurriculumSet();
    }

    @JsonIgnore
    public Day getDay() {
        if (period == null) {
            return null;
        }
        return period.getDay();
    }

    @JsonIgnore
    public int getTimeslotIndex() {
        if (period == null) {
            return Integer.MIN_VALUE;
        }
        return period.getTimeslot().getTimeslotIndex();
    }

    @JsonIgnore
    public int getUnavailablePeriodPenaltyCount(CourseSchedule schedule) {
        if (unavailablePeriodPenaltyCount == -1) {
            unavailablePeriodPenaltyCount = 0;
            for (var penalty : schedule.getUnavailablePeriodPenaltyList()) {
                if (penalty.getCourse().equals(course)) {
                    unavailablePeriodPenaltyCount++;
                }
            }
        }
        return unavailablePeriodPenaltyCount;
    }

    @Override
    public String toString() {
        return course + "-" + lectureIndexInCourse;
    }

}
