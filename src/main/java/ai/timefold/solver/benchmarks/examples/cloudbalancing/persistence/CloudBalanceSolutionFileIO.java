package ai.timefold.solver.benchmarks.examples.cloudbalancing.persistence;

import ai.timefold.solver.benchmarks.examples.cloudbalancing.domain.CloudBalance;
import ai.timefold.solver.jackson.impl.domain.solution.JacksonSolutionFileIO;

public class CloudBalanceSolutionFileIO extends JacksonSolutionFileIO<CloudBalance> {

    public CloudBalanceSolutionFileIO() {
        super(CloudBalance.class);
    }
}
