package ai.timefold.solver.benchmarks.examples.machinereassignment.app;

import java.util.Collections;
import java.util.Set;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.benchmarks.examples.machinereassignment.domain.MachineReassignment;
import ai.timefold.solver.benchmarks.examples.machinereassignment.persistence.MachineReassignmentImporter;
import ai.timefold.solver.benchmarks.examples.machinereassignment.persistence.MachineReassignmentSolutionFileIO;
import ai.timefold.solver.core.api.domain.solution.SolutionFileIO;

public class MachineReassignmentApp extends CommonApp<MachineReassignment> {

    public static final String SOLVER_CONFIG =
            "ai/timefold/solver/benchmarks/examples/machinereassignment/machineReassignmentSolverConfig.xml";

    public static final String DATA_DIR_NAME = "machinereassignment";

    public static void main(String[] args) {
        var solution = new MachineReassignmentApp().solve("model_a2_1.json");
        System.out.println("Done: " + solution);
    }

    public MachineReassignmentApp() {
        super("Machine reassignment",
                "Official competition name: Google ROADEF 2012 - Machine reassignment\n\n" +
                        "Reassign processes to machines.",
                SOLVER_CONFIG, DATA_DIR_NAME);
    }

    @Override
    public SolutionFileIO<MachineReassignment> createSolutionFileIO() {
        return new MachineReassignmentSolutionFileIO();
    }

    @Override
    protected Set<AbstractSolutionImporter<MachineReassignment>> createSolutionImporters() {
        return Collections.singleton(new MachineReassignmentImporter());
    }

}
