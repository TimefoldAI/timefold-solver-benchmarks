package ai.timefold.solver.benchmarks.examples.travelingtournament.persistence;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.OpenDataFilesTest;
import ai.timefold.solver.benchmarks.examples.travelingtournament.app.TravelingTournamentApp;
import ai.timefold.solver.benchmarks.examples.travelingtournament.domain.TravelingTournament;

class TravelingTournamentOpenDataFilesTest extends OpenDataFilesTest<TravelingTournament> {

    @Override
    protected CommonApp<TravelingTournament> createCommonApp() {
        return new TravelingTournamentApp();
    }
}
