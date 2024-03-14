package ai.timefold.solver.benchmarks.examples.tennis.domain;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Team.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Team extends AbstractPersistable {

    private String name;

    public Team() {
    }

    public Team(long id, String name) {
        super(id);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name == null ? super.toString() : name;
    }

}
