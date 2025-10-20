package ai.timefold.solver.benchmarks.examples.meetingscheduling.app;

import ai.timefold.solver.benchmarks.examples.common.app.SolverSmokeTest;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.MeetingSchedule;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

import java.util.stream.Stream;

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
                        HardMediumSoftScore.of(-9, -442, -9375),
                        HardMediumSoftScore.of(-13, -412, -8573)));
    }
}
