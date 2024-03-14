package ai.timefold.solver.benchmarks.examples.nurserostering.domain.solver;

import ai.timefold.solver.benchmarks.examples.nurserostering.domain.NurseRoster;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.ShiftAssignment;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.ShiftDate;
import ai.timefold.solver.core.api.domain.entity.PinningFilter;

public class ShiftAssignmentPinningFilter implements
        PinningFilter<NurseRoster, ShiftAssignment> {

    @Override
    public boolean accept(NurseRoster nurseRoster, ShiftAssignment shiftAssignment) {
        ShiftDate shiftDate = shiftAssignment.getShift().getShiftDate();
        return !nurseRoster.getNurseRosterParametrization().isInPlanningWindow(shiftDate);
    }

}
