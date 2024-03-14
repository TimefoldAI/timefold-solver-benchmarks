package ai.timefold.solver.benchmarks.examples.nurserostering.persistence;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.OpenDataFilesTest;
import ai.timefold.solver.benchmarks.examples.nurserostering.app.NurseRosteringApp;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.NurseRoster;

class NurseRosteringOpenDataFilesTest extends OpenDataFilesTest<NurseRoster> {

    @Override
    protected CommonApp<NurseRoster> createCommonApp() {
        return new NurseRosteringApp();
    }
}
