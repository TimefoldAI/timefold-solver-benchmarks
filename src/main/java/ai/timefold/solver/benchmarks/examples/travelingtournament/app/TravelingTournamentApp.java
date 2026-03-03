package ai.timefold.solver.benchmarks.examples.travelingtournament.app;

import java.util.Collections;
import java.util.Set;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.benchmarks.examples.travelingtournament.domain.TravelingTournament;
import ai.timefold.solver.benchmarks.examples.travelingtournament.persistence.TravelingTournamentImporter;
import ai.timefold.solver.benchmarks.examples.travelingtournament.persistence.TravelingTournamentSolutionFileIO;
import ai.timefold.solver.core.api.domain.solution.SolutionFileIO;

/**
 * WARNING: This is an old, complex, tailored example. You're probably better off with one of the other examples.
 */
public class TravelingTournamentApp extends CommonApp<TravelingTournament> {

    public static final String SOLVER_CONFIG =
            "ai/timefold/solver/benchmarks/examples/travelingtournament/travelingTournamentSolverConfig.xml";

    public static final String DATA_DIR_NAME = "travelingtournament";

    public static void main(String[] args) {
        var solution = new TravelingTournamentApp().solve("4-super14.json");
        System.out.println("Done: " + solution);
    }

    public TravelingTournamentApp() {
        super("Traveling tournament",
                "Official competition name: TTP - Traveling tournament problem\n\n" +
                        "Assign sport matches to days. Minimize the distance travelled.",
                SOLVER_CONFIG, DATA_DIR_NAME);
    }

    @Override
    public SolutionFileIO<TravelingTournament> createSolutionFileIO() {
        return new TravelingTournamentSolutionFileIO();
    }

    @Override
    protected Set<AbstractSolutionImporter<TravelingTournament>> createSolutionImporters() {
        return Collections.singleton(new TravelingTournamentImporter());
    }

}
