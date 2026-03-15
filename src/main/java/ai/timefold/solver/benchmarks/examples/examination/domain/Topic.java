package ai.timefold.solver.benchmarks.examples.examination.domain;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.stream.Collectors;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class Topic extends AbstractPersistable {

    private int duration; // in minutes
    private SequencedSet<Student> studentSet;

    // Calculated during initialization, not modified during score calculation.
    private boolean frontLoadLarge;
    private SequencedSet<Topic> coincidenceTopicSet = null;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public SequencedSet<Student> getStudentSet() {
        return studentSet;
    }

    public void setStudentSet(SequencedSet<Student> studentSet) {
        this.studentSet = studentSet;
    }

    @JsonIgnore
    public int getStudentSize() {
        return studentSet.size();
    }

    public boolean isFrontLoadLarge() {
        return frontLoadLarge;
    }

    public void setFrontLoadLarge(boolean frontLoadLarge) {
        this.frontLoadLarge = frontLoadLarge;
    }

    public SequencedSet<Topic> getCoincidenceTopicSet() {
        return coincidenceTopicSet;
    }

    public void setCoincidenceTopicSet(SequencedSet<Topic> coincidenceTopicSet) {
        this.coincidenceTopicSet = coincidenceTopicSet;
    }

    public boolean hasCoincidenceTopic() {
        return coincidenceTopicSet != null;
    }

    @Override
    public String toString() {
        return id == null ? "no id" : Long.toString(id);
    }

    // ************************************************************************
    // With methods
    // ************************************************************************

    public Topic withId(long id) {
        this.setId(id);
        return this;
    }

    public Topic withDuration(int duration) {
        this.setDuration(duration);
        return this;
    }

    public Topic withStudents(Student... students) {
        this.setStudentSet(Arrays.stream(students).collect(Collectors.toCollection(LinkedHashSet::new)));
        return this;
    }

    public Topic withFrontLoadLarge(boolean frontLoadLarge) {
        this.setFrontLoadLarge(frontLoadLarge);
        return this;
    }

    public Topic withCoincidenceTopicSet(SequencedSet<Topic> coincidenceTopicSet) {
        this.setCoincidenceTopicSet(coincidenceTopicSet);
        return this;
    }
}
