package ai.timefold.solver.benchmarks.examples.meetingscheduling.app;

import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.examples.common.app.SolverSmokeTest;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.MeetingSchedule;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

class MeetingSchedulingSmokeTest extends SolverSmokeTest<MeetingSchedule, HardMediumSoftScore> {

    private static final String UNSOLVED_DATA_FILE = "data/meetingscheduling/unsolved/50meetings-160timegrains-5rooms.json";

    @Override
    protected MeetingSchedulingApp createCommonApp() {
        return new MeetingSchedulingApp();
    }

    @Override
    protected Stream<TestData<HardMediumSoftScore>> testData() {
        return Stream.of(
                TestData.of(UNSOLVED_DATA_FILE,
                        HardMediumSoftScore.of(-29, -344, -9227),
                        HardMediumSoftScore.of(-116, -143, -5094)));
    }
}
