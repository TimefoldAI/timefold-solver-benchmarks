package ai.timefold.solver.benchmarks.examples.machinereassignment.solver.solution.initializer;

import ai.timefold.solver.benchmarks.examples.machinereassignment.domain.MachineReassignment;
import ai.timefold.solver.benchmarks.examples.machinereassignment.domain.MrMachine;
import ai.timefold.solver.benchmarks.examples.machinereassignment.domain.MrProcessAssignment;
import ai.timefold.solver.core.api.solver.phase.PhaseCommand;
import ai.timefold.solver.core.api.solver.phase.PhaseCommandContext;
import ai.timefold.solver.core.preview.api.domain.metamodel.PlanningVariableMetaModel;
import ai.timefold.solver.core.preview.api.move.builtin.Moves;

public class ToOriginalMachineSolutionInitializer implements PhaseCommand<MachineReassignment> {

    @Override
    public void changeWorkingSolution(PhaseCommandContext<MachineReassignment> phaseCommandContext) {
        MachineReassignment machineReassignment = phaseCommandContext.getWorkingSolution();
        initializeProcessAssignmentList(phaseCommandContext, machineReassignment);
    }

    private void initializeProcessAssignmentList(PhaseCommandContext<MachineReassignment> phaseCommandContext,
            MachineReassignment machineReassignment) {
        var variableMetaModel =
                (PlanningVariableMetaModel<MachineReassignment, MrProcessAssignment, MrMachine>) phaseCommandContext
                        .getSolutionMetaModel().entity(MrProcessAssignment.class).<MrMachine> variable("machine");
        for (MrProcessAssignment processAssignment : machineReassignment.getProcessAssignmentList()) {
            MrMachine originalMachine = processAssignment.getOriginalMachine();
            MrMachine machine = originalMachine == null ? machineReassignment.getMachineList().get(0) : originalMachine;
            phaseCommandContext.execute(Moves.change(variableMetaModel, processAssignment, machine));
        }
    }
}
