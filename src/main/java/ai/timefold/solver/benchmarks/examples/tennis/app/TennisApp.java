package ai.timefold.solver.benchmarks.examples.tennis.app;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.tennis.domain.TennisSolution;
import ai.timefold.solver.benchmarks.examples.tennis.persistence.TennisSolutionFileIO;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

public class TennisApp extends CommonApp<TennisSolution> {

    public static final String SOLVER_CONFIG = "ai/timefold/solver/benchmarks/examples/tennis/tennisSolverConfig.xml";

    public static final String DATA_DIR_NAME = "tennis";

    public static void main(String[] args) {
        var solution = new TennisApp().solve("munich-7teams.json");
        System.out.println("Done: " + solution);
    }

    public TennisApp() {
        super("Tennis club scheduling",
                "Assign available spots to teams.\n\n" +
                        "Each team must play an almost equal number of times.\n" +
                        "Each team must play against each other team an almost equal number of times.",
                SOLVER_CONFIG, DATA_DIR_NAME);
    }

    @Override
    public SolutionFileIO<TennisSolution> createSolutionFileIO() {
        return new TennisSolutionFileIO();
    }

}
