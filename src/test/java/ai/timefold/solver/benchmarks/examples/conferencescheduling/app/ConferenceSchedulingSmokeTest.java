
package ai.timefold.solver.benchmarks.examples.conferencescheduling.app;

import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.examples.common.app.SolverSmokeTest;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceSolution;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;

class ConferenceSchedulingSmokeTest extends SolverSmokeTest<ConferenceSolution, HardSoftScore> {

    private static final String UNSOLVED_DATA_FILE = "data/conferencescheduling/unsolved/72talks-12timeslots-10rooms.json";

    @Override
    protected ConferenceSchedulingApp createCommonApp() {
        return new ConferenceSchedulingApp();
    }

    @Override
    protected Stream<TestData<HardSoftScore>> testData() {
        return Stream.of(
                TestData.of(UNSOLVED_DATA_FILE,
                        HardSoftScore.ofSoft(-1025250),
                        HardSoftScore.ofSoft(-1100400)));
    }
}
