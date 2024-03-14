package ai.timefold.solver.benchmarks.examples.projectjobscheduling.app;

import java.util.Collections;
import java.util.Set;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.benchmarks.examples.projectjobscheduling.domain.Schedule;
import ai.timefold.solver.benchmarks.examples.projectjobscheduling.persistence.ProjectJobSchedulingImporter;
import ai.timefold.solver.benchmarks.examples.projectjobscheduling.persistence.ProjectJobSchedulingSolutionFileIO;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

public class ProjectJobSchedulingApp
        extends CommonApp<Schedule> {

    public static final String SOLVER_CONFIG =
            "ai/timefold/solver/benchmarks/examples/projectjobscheduling/projectJobSchedulingSolverConfig.xml";

    public static final String DATA_DIR_NAME = "projectjobscheduling";

    public static void main(String[] args) {
        var solution = new ProjectJobSchedulingApp().solve("B-7.json");
        System.out.println("Done: " + solution);
    }

    public ProjectJobSchedulingApp() {
        super("Project job scheduling",
                "Official competition name:" +
                        " multi-mode resource-constrained multi-project scheduling problem (MRCMPSP)\n\n" +
                        "Schedule all jobs in time and execution mode.\n\n" +
                        "Minimize project delays.",
                SOLVER_CONFIG, DATA_DIR_NAME);
    }

    @Override
    public SolutionFileIO<Schedule> createSolutionFileIO() {
        return new ProjectJobSchedulingSolutionFileIO();
    }

    @Override
    protected Set<AbstractSolutionImporter<Schedule>> createSolutionImporters() {
        return Collections.singleton(new ProjectJobSchedulingImporter());
    }

}
