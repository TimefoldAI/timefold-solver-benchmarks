package ai.timefold.solver.benchmarks.examples.nurserostering.app;

import java.math.BigDecimal;
import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.examples.common.app.SolverSmokeTest;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.NurseRoster;
import ai.timefold.solver.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;

class NurseRosteringSmokeTest extends SolverSmokeTest<NurseRoster, HardSoftBigDecimalScore> {

    private static final String UNSOLVED_DATA_FILE = "data/nurserostering/unsolved/medium_late01_initialized.json";

    @Override
    protected NurseRosteringApp createCommonApp() {
        return new NurseRosteringApp();
    }

    @Override
    protected Stream<TestData<HardSoftBigDecimalScore>> testData() {
        return Stream.of(
                TestData.of(UNSOLVED_DATA_FILE,
                        HardSoftBigDecimalScore.ofSoft(BigDecimal.valueOf(-384.0724)),
                        HardSoftBigDecimalScore.ofSoft(BigDecimal.valueOf(-391.3628))));
    }
}
