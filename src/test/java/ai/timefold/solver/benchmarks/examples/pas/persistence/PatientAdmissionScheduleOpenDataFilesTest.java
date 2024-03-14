package ai.timefold.solver.benchmarks.examples.pas.persistence;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.OpenDataFilesTest;
import ai.timefold.solver.benchmarks.examples.pas.app.PatientAdmissionScheduleApp;
import ai.timefold.solver.benchmarks.examples.pas.domain.PatientAdmissionSchedule;

class PatientAdmissionScheduleOpenDataFilesTest extends OpenDataFilesTest<PatientAdmissionSchedule> {

    @Override
    protected CommonApp<PatientAdmissionSchedule> createCommonApp() {
        return new PatientAdmissionScheduleApp();
    }
}
