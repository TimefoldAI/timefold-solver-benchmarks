package ai.timefold.solver.benchmarks.examples.conferencescheduling.persistence;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractJsonSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceSolution;

public class ConferenceSchedulingSolutionFileIO extends
        AbstractJsonSolutionFileIO<ConferenceSolution> {

    public ConferenceSchedulingSolutionFileIO() {
        super(ConferenceSolution.class);
    }

}
