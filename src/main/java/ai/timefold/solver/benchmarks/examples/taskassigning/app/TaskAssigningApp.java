package ai.timefold.solver.benchmarks.examples.taskassigning.app;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.taskassigning.domain.TaskAssigningSolution;
import ai.timefold.solver.benchmarks.examples.taskassigning.persistence.TaskAssigningSolutionFileIO;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

public class TaskAssigningApp extends CommonApp<TaskAssigningSolution> {

    public static final String SOLVER_CONFIG =
            "ai/timefold/solver/benchmarks/examples/taskassigning/taskAssigningSolverConfig.xml";

    public static final String DATA_DIR_NAME = "taskassigning";

    public static void main(String[] args) {
        var solution = new TaskAssigningApp().solve("500tasks-20employees.json");
        System.out.println("Done: " + solution);
    }

    public TaskAssigningApp() {
        super("Task assigning",
                "Assign tasks to employees in a sequence.\n\n"
                        + "Match skills and affinity.\n"
                        + "Prioritize critical tasks.\n"
                        + "Minimize the makespan.",
                SOLVER_CONFIG, DATA_DIR_NAME);
    }

    @Override
    public SolutionFileIO<TaskAssigningSolution> createSolutionFileIO() {
        return new TaskAssigningSolutionFileIO();
    }

}
