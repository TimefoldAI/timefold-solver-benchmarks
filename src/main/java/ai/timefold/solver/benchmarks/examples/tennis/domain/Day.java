package ai.timefold.solver.benchmarks.examples.tennis.domain;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Day.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Day extends AbstractPersistable {

    private int dateIndex;

    public Day() {
    }

    public Day(long id, int dateIndex) {
        super(id);
        this.dateIndex = dateIndex;
    }

    public int getDateIndex() {
        return dateIndex;
    }

    public void setDateIndex(int dateIndex) {
        this.dateIndex = dateIndex;
    }

}
