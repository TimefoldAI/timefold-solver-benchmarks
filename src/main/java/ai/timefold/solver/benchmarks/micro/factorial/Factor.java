package ai.timefold.solver.benchmarks.micro.factorial;

import java.util.List;

public class Factor<T> {
    private final String name;
    private final List<Level<T>> levelList;

    public Factor(String name, List<Level<T>> levelList) {
        this.name = name;
        this.levelList = levelList;
    }

    public String getName() {
        return name;
    }

    public List<Level<T>> getLevelList() {
        return levelList;
    }
}
