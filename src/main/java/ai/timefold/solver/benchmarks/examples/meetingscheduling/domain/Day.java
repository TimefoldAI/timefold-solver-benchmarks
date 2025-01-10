package ai.timefold.solver.benchmarks.examples.meetingscheduling.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class Day extends AbstractPersistable
        implements Comparable<Day> {

    private int dayOfYear;

    public Day() {
    }

    public Day(long id, int dayOfYear) {
        super(id);
        this.dayOfYear = dayOfYear;
    }

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("E", Locale.ENGLISH);

    public int getDayOfYear() {
        return dayOfYear;
    }

    public void setDayOfYear(int dayOfYear) {
        this.dayOfYear = dayOfYear;
    }

    @JsonIgnore
    public String getDateString() {
        return DAY_FORMATTER.format(toDate());
    }

    @JsonIgnore
    public LocalDate toDate() {
        return LocalDate.ofYearDay(LocalDate.now().getYear(), dayOfYear);
    }

    @Override
    public String toString() {
        return getDateString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;

        Day day = (Day) other;

        return dayOfYear == day.dayOfYear;
    }

    @Override
    public int hashCode() {
        return dayOfYear;
    }

    @Override
    public int compareTo(Day o) {
        return Integer.compare(this.dayOfYear, o.dayOfYear);
    }
}
