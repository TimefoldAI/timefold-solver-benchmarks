package ai.timefold.solver.benchmarks.examples.curriculumcourse.domain;

import static java.util.Objects.requireNonNull;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Period.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Period extends AbstractPersistable {

    private Day day;
    private Timeslot timeslot;
    private int unavailablePeriodPenaltyCount = -1;

    public Period() {
    }

    public Period(long id, Day day, Timeslot timeslot) {
        super(id);
        this.day = requireNonNull(day);
        day.getPeriodList().add(this);
        this.timeslot = requireNonNull(timeslot);
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    @JsonIgnore
    public int getUnavailablePeriodPenaltyCount(CourseSchedule schedule) {
        if (unavailablePeriodPenaltyCount == -1) {
            unavailablePeriodPenaltyCount = 0;
            for (var penalty : schedule.getUnavailablePeriodPenaltyList()) {
                if (penalty.getPeriod().equals(this)) {
                    unavailablePeriodPenaltyCount++;
                }
            }
        }
        return unavailablePeriodPenaltyCount;
    }

    @Override
    public String toString() {
        return day + "-" + timeslot;
    }

}
