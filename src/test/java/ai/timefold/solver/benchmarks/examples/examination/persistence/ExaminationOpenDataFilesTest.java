package ai.timefold.solver.benchmarks.examples.examination.persistence;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.OpenDataFilesTest;
import ai.timefold.solver.benchmarks.examples.examination.app.ExaminationApp;
import ai.timefold.solver.benchmarks.examples.examination.domain.Examination;

class ExaminationOpenDataFilesTest extends OpenDataFilesTest<Examination> {

    @Override
    protected CommonApp<Examination> createCommonApp() {
        return new ExaminationApp();
    }
}
