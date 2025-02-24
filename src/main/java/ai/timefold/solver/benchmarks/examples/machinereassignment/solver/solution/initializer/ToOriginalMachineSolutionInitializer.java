package ai.timefold.solver.benchmarks.examples.machinereassignment.solver.solution.initializer;

import ai.timefold.solver.benchmarks.examples.machinereassignment.domain.MachineReassignment;
import ai.timefold.solver.benchmarks.examples.machinereassignment.domain.MrMachine;
import ai.timefold.solver.benchmarks.examples.machinereassignment.domain.MrProcessAssignment;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.api.solver.phase.PhaseCommand;

import java.util.function.BooleanSupplier;

public class ToOriginalMachineSolutionInitializer implements PhaseCommand<MachineReassignment> {

    @Override
    public void changeWorkingSolution(ScoreDirector<MachineReassignment> scoreDirector, BooleanSupplier isPhaseTerminated) {
        MachineReassignment machineReassignment = scoreDirector.getWorkingSolution();
        initializeProcessAssignmentList(scoreDirector, machineReassignment);
    }

    private void initializeProcessAssignmentList(ScoreDirector<MachineReassignment> scoreDirector,
            MachineReassignment machineReassignment) {
        for (MrProcessAssignment processAssignment : machineReassignment.getProcessAssignmentList()) {
            MrMachine originalMachine = processAssignment.getOriginalMachine();
            MrMachine machine = originalMachine == null ? machineReassignment.getMachineList().get(0) : originalMachine;
            scoreDirector.beforeVariableChanged(processAssignment, "machine");
            processAssignment.setMachine(machine);
            scoreDirector.afterVariableChanged(processAssignment, "machine");
            scoreDirector.triggerVariableListeners();
        }
    }

}
