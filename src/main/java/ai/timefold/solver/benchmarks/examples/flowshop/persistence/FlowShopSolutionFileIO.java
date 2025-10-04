package ai.timefold.solver.benchmarks.examples.flowshop.persistence;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractJsonSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.flowshop.domain.JobScheduleSolution;

public final class FlowShopSolutionFileIO extends AbstractJsonSolutionFileIO<JobScheduleSolution> {

    public FlowShopSolutionFileIO() {
        super(JobScheduleSolution.class);
    }

}
