package ai.timefold.solver.benchmarks.examples.conferencescheduling.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class Timeslot extends AbstractPersistable {

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    private Set<TalkType> talkTypeSet;
    private Set<String> tagSet;

    // Cached
    private int durationInMinutes;

    public Timeslot() {
    }

    public Timeslot(long id) {
        super(id);
    }

    @JsonIgnore
    public LocalDate getDate() {
        return startDateTime.toLocalDate();
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public boolean overlapsTime(Timeslot other) {
        if (this == other) {
            return true;
        }
        return startDateTime.isBefore(other.endDateTime) && other.startDateTime.isBefore(endDateTime);
    }

    public int getOverlapInMinutes(Timeslot other) {
        if (this == other) {
            return durationInMinutes;
        }
        LocalDateTime startMaximum = (startDateTime.isBefore(other.startDateTime)) ? other.startDateTime : startDateTime;
        LocalDateTime endMinimum = (endDateTime.isBefore(other.endDateTime)) ? endDateTime : other.endDateTime;
        return (int) Duration.between(startMaximum, endMinimum).toMinutes();
    }

    public boolean startsAfter(Timeslot other) {
        return !other.endDateTime.isAfter(startDateTime);
    }

    public boolean endsBefore(Timeslot other) {
        return !endDateTime.isAfter(other.startDateTime);
    }

    public boolean isOnSameDayAs(Timeslot other) {
        return startDateTime.toLocalDate().equals(other.getStartDateTime().toLocalDate());
    }

    public boolean pauseExists(Timeslot other, int pauseInMinutes) {
        if (this.overlapsTime(other)) {
            return false;
        }
        if (!this.isOnSameDayAs(other)) {
            return true;
        }
        Duration pause = startsAfter(other) ? Duration.between(other.getEndDateTime(), getStartDateTime())
                : Duration.between(getEndDateTime(), other.getStartDateTime());
        return pause.toMinutes() >= pauseInMinutes;
    }

    @Override
    public String toString() {
        return startDateTime + "-" + endDateTime.toLocalTime();
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
        durationInMinutes = (startDateTime == null || endDateTime == null) ? 0
                : (int) Duration.between(startDateTime, endDateTime).toMinutes();
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
        durationInMinutes = (startDateTime == null || endDateTime == null) ? 0
                : (int) Duration.between(startDateTime, endDateTime).toMinutes();
    }

    public Set<TalkType> getTalkTypeSet() {
        return talkTypeSet;
    }

    public void setTalkTypeSet(Set<TalkType> talkTypeSet) {
        this.talkTypeSet = talkTypeSet;
    }

    public Set<String> getTagSet() {
        return tagSet;
    }

    public void setTagSet(Set<String> tagSet) {
        this.tagSet = tagSet;
    }

    // ************************************************************************
    // With methods
    // ************************************************************************

    public Timeslot withStartDateTime(LocalDateTime startDateTime) {
        setStartDateTime(startDateTime);
        return this;
    }

    public Timeslot withEndDateTime(LocalDateTime endDateTime) {
        setEndDateTime(endDateTime);
        return this;
    }

    public Timeslot withTalkTypeSet(Set<TalkType> talkTypeSet) {
        this.talkTypeSet = talkTypeSet;
        return this;
    }

    public Timeslot withTagSet(Set<String> tagSet) {
        this.tagSet = tagSet;
        return this;
    }

}
