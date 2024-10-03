package ai.timefold.solver.benchmarks.micro.factorial.planning;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Factor {
    private final String name;
    private final List<Level> levelList;

    @JsonCreator
    public Factor(@JsonProperty("name") String name, @JsonProperty("levels") List<String> levels) {
        this.name = name;
        this.levelList = new ArrayList<>(levels.size());
        levels.forEach(level -> this.levelList.add(new Level(this, level)));
    }

    public String getName() {
        return name;
    }

    public List<Level> getLevelList() {
        return levelList;
    }
}
