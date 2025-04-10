package ai.timefold.solver.benchmarks.examples.nurserostering.app;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.NurseRoster;
import ai.timefold.solver.benchmarks.examples.nurserostering.persistence.NurseRosterSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.nurserostering.persistence.NurseRosteringImporter;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

public class NurseRosteringApp extends CommonApp<NurseRoster> {

    public static final String SOLVER_CONFIG =
            "ai/timefold/solver/benchmarks/examples/nurserostering/nurseRosteringSolverConfig.xml";

    public static final String DATA_DIR_NAME = "nurserostering";

    public static void main(String[] args) {
        var app = new NurseRosteringApp();
        var solution = app.solve("medium_late01.json");
        app.createSolutionFileIO().write(solution, new File("medium_late01_initialized.json"));
        System.out.println("Done: " + solution);
    }

    public NurseRosteringApp() {
        super("Nurse rostering",
                "Official competition name: INRC2010 - Nurse rostering\n\n" +
                        "Assign shifts to nurses.",
                SOLVER_CONFIG, DATA_DIR_NAME);
    }

    @Override
    public SolutionFileIO<NurseRoster> createSolutionFileIO() {
        return new NurseRosterSolutionFileIO();
    }

    @Override
    protected Set<AbstractSolutionImporter<NurseRoster>> createSolutionImporters() {
        return Collections.singleton(new NurseRosteringImporter());
    }

}
