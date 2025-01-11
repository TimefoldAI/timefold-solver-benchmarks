package ai.timefold.solver.benchmarks.examples.tsp.app;

import java.util.Collections;
import java.util.Set;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;
import ai.timefold.solver.benchmarks.examples.tsp.persistence.TspImporter;
import ai.timefold.solver.benchmarks.examples.tsp.persistence.TspSolutionFileIO;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

public class TspApp extends CommonApp<TspSolution> {

    public static final String SOLVER_CONFIG = "ai/timefold/solver/benchmarks/examples/tsp/tspSolverConfig.xml";

    public static final String DATA_DIR_NAME = "tsp";

    public static void main(String[] args) {
        var solution = new TspApp().solve("europe40.json");
        System.out.println("Done: " + solution);
    }

    public TspApp() {
        super("Traveling salesman",
                "Official competition name: TSP - Traveling salesman problem\n\n" +
                        "Determine the order in which to visit all cities.\n\n" +
                        "Find the shortest route to visit all cities.",
                SOLVER_CONFIG, DATA_DIR_NAME);
    }

    @Override
    public SolutionFileIO<TspSolution> createSolutionFileIO() {
        return new TspSolutionFileIO();
    }

    @Override
    protected Set<AbstractSolutionImporter<TspSolution>> createSolutionImporters() {
        return Collections.singleton(new TspImporter());
    }

}
