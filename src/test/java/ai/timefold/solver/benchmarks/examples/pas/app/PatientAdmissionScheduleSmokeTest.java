package ai.timefold.solver.benchmarks.examples.pas.app;

import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.examples.common.app.SolverSmokeTest;
import ai.timefold.solver.benchmarks.examples.pas.domain.PatientAdmissionSchedule;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.ConstraintStreamImplType;

class PatientAdmissionScheduleSmokeTest
        extends SolverSmokeTest<PatientAdmissionSchedule, HardMediumSoftScore> {

    private static final String UNSOLVED_DATA_FILE = "data/pas/unsolved/testdata01.json";

    @Override
    protected PatientAdmissionScheduleApp createCommonApp() {
        return new PatientAdmissionScheduleApp();
    }

    @Override
    protected Stream<TestData<HardMediumSoftScore>> testData() {
        return Stream.of(
                TestData.of(ConstraintStreamImplType.BAVET, UNSOLVED_DATA_FILE,
                        HardMediumSoftScore.ofSoft(-7362),
                        HardMediumSoftScore.ofSoft(-7378)));
    }
}
