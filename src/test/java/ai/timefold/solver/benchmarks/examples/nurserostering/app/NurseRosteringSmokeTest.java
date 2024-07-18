package ai.timefold.solver.benchmarks.examples.nurserostering.app;

import java.math.BigDecimal;
import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.examples.common.app.SolverSmokeTest;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.NurseRoster;
import ai.timefold.solver.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import ai.timefold.solver.core.api.score.stream.ConstraintStreamImplType;

class NurseRosteringSmokeTest extends SolverSmokeTest<NurseRoster, HardSoftBigDecimalScore> {

    private static final String UNSOLVED_DATA_FILE = "data/nurserostering/unsolved/medium_late01_initialized.json";

    @Override
    protected NurseRosteringApp createCommonApp() {
        return new NurseRosteringApp();
    }

    @Override
    protected Stream<TestData<HardSoftBigDecimalScore>> testData() {
        return Stream.of(
                TestData.of(ConstraintStreamImplType.BAVET, UNSOLVED_DATA_FILE,
                        HardSoftBigDecimalScore.ofSoft(BigDecimal.valueOf(-567.9833)),
                        HardSoftBigDecimalScore.ofSoft(BigDecimal.valueOf(-567.9833))));
    }
}
