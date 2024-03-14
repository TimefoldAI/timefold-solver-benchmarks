package ai.timefold.solver.benchmarks.examples.pas.app;

import java.util.Collections;
import java.util.Set;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.benchmarks.examples.pas.domain.PatientAdmissionSchedule;
import ai.timefold.solver.benchmarks.examples.pas.persistence.PatientAdmissionScheduleImporter;
import ai.timefold.solver.benchmarks.examples.pas.persistence.PatientAdmissionScheduleSolutionFileIO;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

public class PatientAdmissionScheduleApp
        extends CommonApp<PatientAdmissionSchedule> {

    public static final String SOLVER_CONFIG =
            "ai/timefold/solver/benchmarks/examples/pas/patientAdmissionScheduleSolverConfig.xml";

    public static final String DATA_DIR_NAME = "pas";

    public static void main(String[] args) {
        var solution = new PatientAdmissionScheduleApp().solve("testdata12.json");
        System.out.println("Done: " + solution);
    }

    public PatientAdmissionScheduleApp() {
        super("Hospital bed planning",
                "Official competition name: PAS - Patient admission scheduling\n\n" +
                        "Assign patients to beds.",
                SOLVER_CONFIG, DATA_DIR_NAME);
    }

    @Override
    public SolutionFileIO<PatientAdmissionSchedule> createSolutionFileIO() {
        return new PatientAdmissionScheduleSolutionFileIO();
    }

    @Override
    protected Set<AbstractSolutionImporter<PatientAdmissionSchedule>> createSolutionImporters() {
        return Collections.singleton(new PatientAdmissionScheduleImporter());
    }

}
