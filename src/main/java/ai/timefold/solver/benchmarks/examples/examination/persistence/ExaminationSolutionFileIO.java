package ai.timefold.solver.benchmarks.examples.examination.persistence;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractJsonSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.examination.domain.Examination;

public class ExaminationSolutionFileIO
        extends AbstractJsonSolutionFileIO<Examination> {

    public ExaminationSolutionFileIO() {
        super(Examination.class);
    }
}
