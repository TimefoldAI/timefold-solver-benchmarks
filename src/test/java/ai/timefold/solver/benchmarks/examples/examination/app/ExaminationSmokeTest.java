package ai.timefold.solver.benchmarks.examples.examination.app;

import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.examples.common.app.SolverSmokeTest;
import ai.timefold.solver.benchmarks.examples.examination.domain.Examination;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;

class ExaminationSmokeTest extends SolverSmokeTest<Examination, HardSoftScore> {

    private static final String UNSOLVED_DATA_FILE = "data/examination/unsolved/exam_comp_set5.json";

    @Override
    protected ExaminationApp createCommonApp() {
        return new ExaminationApp();
    }

    @Override
    protected Stream<TestData<HardSoftScore>> testData() {
        return Stream.of(
                TestData.of(UNSOLVED_DATA_FILE,
                        HardSoftScore.ofSoft(-4195),
                        HardSoftScore.ofSoft(-4312)));
    }
}
