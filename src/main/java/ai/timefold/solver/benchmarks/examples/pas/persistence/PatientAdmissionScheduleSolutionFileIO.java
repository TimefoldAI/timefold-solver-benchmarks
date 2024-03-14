package ai.timefold.solver.benchmarks.examples.pas.persistence;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractJsonSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.pas.domain.PatientAdmissionSchedule;

public class PatientAdmissionScheduleSolutionFileIO
        extends AbstractJsonSolutionFileIO<PatientAdmissionSchedule> {

    public PatientAdmissionScheduleSolutionFileIO() {
        super(PatientAdmissionSchedule.class);
    }
}
