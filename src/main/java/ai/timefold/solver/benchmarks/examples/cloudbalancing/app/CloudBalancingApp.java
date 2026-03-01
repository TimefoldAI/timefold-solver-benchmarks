package ai.timefold.solver.benchmarks.examples.cloudbalancing.app;

import ai.timefold.solver.benchmarks.examples.cloudbalancing.domain.CloudBalance;
import ai.timefold.solver.benchmarks.examples.cloudbalancing.persistence.CloudBalanceSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.core.api.domain.solution.SolutionFileIO;

public class CloudBalancingApp extends CommonApp<CloudBalance> {

    public static final String SOLVER_CONFIG =
            "ai/timefold/solver/benchmarks/examples/cloudbalancing/cloudBalancingSolverConfig.xml";

    public static final String DATA_DIR_NAME = "cloudbalancing";

    public static void main(String[] args) {
        var solution = new CloudBalancingApp().solve("1600computers-4800processes.json");
        System.out.println("Done: " + solution);
    }

    public CloudBalancingApp() {
        super("Cloud balancing",
                "Assign processes to computers.\n\n" +
                        "Each computer must have enough hardware to run all of its processes.\n" +
                        "Each used computer inflicts a maintenance cost.",
                SOLVER_CONFIG, DATA_DIR_NAME);
    }

    @Override
    public SolutionFileIO<CloudBalance> createSolutionFileIO() {
        return new CloudBalanceSolutionFileIO();
    }

}
