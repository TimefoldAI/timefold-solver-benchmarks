package ai.timefold.solver.benchmarks.examples.tennis.persistence;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractJsonSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.tennis.domain.TennisSolution;

public class TennisSolutionFileIO
        extends AbstractJsonSolutionFileIO<TennisSolution> {

    public TennisSolutionFileIO() {
        super(TennisSolution.class);
    }
}
