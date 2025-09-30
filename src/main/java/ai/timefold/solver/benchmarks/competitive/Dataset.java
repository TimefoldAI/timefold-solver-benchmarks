package ai.timefold.solver.benchmarks.competitive;

import java.math.BigDecimal;
import java.nio.file.Path;

public interface Dataset<Dataset_> extends Comparable<Dataset_> {

    String name();

    int ordinal();

    // BigDecimal for precision.
    BigDecimal getBestKnownSolution();

    boolean isLarge();

    boolean isBestKnownSolutionOptimal();

    Path getPath();

}
