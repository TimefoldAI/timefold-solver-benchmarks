package ai.timefold.solver.benchmarks.examples.travelingtournament.app;

import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.examples.common.app.SolverSmokeTest;
import ai.timefold.solver.benchmarks.examples.travelingtournament.domain.TravelingTournament;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;

class TravelingTournamentSmokeTest extends SolverSmokeTest<TravelingTournament, HardSoftScore> {

    private static final String UNSOLVED_DATA_FILE = "data/travelingtournament/unsolved/1-nl10.json";

    @Override
    protected TravelingTournamentApp createCommonApp() {
        return new TravelingTournamentApp();
    }

    @Override
    protected Stream<TestData<HardSoftScore>> testData() {
        return Stream.of(
                TestData.of(UNSOLVED_DATA_FILE,
                        HardSoftScore.ofSoft(-72772),
                        HardSoftScore.ofSoft(-72772)));
    }
}
