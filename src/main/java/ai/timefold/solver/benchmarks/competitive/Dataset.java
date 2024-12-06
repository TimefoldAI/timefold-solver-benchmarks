package ai.timefold.solver.benchmarks.competitive;

import java.nio.file.Path;

public interface Dataset {

    String name();

    int ordinal();

    int getBestKnownDistance();

    boolean isLarge();

    Path getPath();

}
