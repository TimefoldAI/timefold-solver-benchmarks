package ai.timefold.solver.benchmarks.examples.flowshop.app;

import java.util.Collections;
import java.util.Set;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.benchmarks.examples.flowshop.domain.JobScheduleSolution;
import ai.timefold.solver.benchmarks.examples.flowshop.persistence.FlowShopSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.flowshop.persistence.TaillardImporter;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

public class FlowShopApp extends CommonApp<JobScheduleSolution> {

    public static final String SOLVER_CONFIG = "ai/timefold/solver/benchmarks/examples/flowshop/flowShopSolverConfig.xml";

    public static final String DATA_DIR_NAME = "flowshop";

    public static void main(String[] args) {
        var solution = new FlowShopApp().solve("Ta001.json");
        System.out.println("Done: " + solution);
    }

    public FlowShopApp() {
        super("FlowShop Scheduling Problem", """
                Assign jobs to a machine.

                All jobs must be assigned.
                
                A machine can run only one job at the time.
                
                A job can only begin on the machine once it has completed on the previous machine or after the prior job has finished.
                """,
                SOLVER_CONFIG, DATA_DIR_NAME);
    }

    @Override
    public SolutionFileIO<JobScheduleSolution> createSolutionFileIO() {
        return new FlowShopSolutionFileIO();
    }

    @Override
    protected Set<AbstractSolutionImporter<JobScheduleSolution>> createSolutionImporters() {
        return Collections.singleton(new TaillardImporter());
    }

}
