package ai.timefold.solver.benchmarks.examples.machinereassignment.persistence;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.OpenDataFilesTest;
import ai.timefold.solver.benchmarks.examples.machinereassignment.app.MachineReassignmentApp;
import ai.timefold.solver.benchmarks.examples.machinereassignment.domain.MachineReassignment;

class MachineReassignmentOpenDataFilesTest extends OpenDataFilesTest<MachineReassignment> {

    @Override
    protected CommonApp<MachineReassignment> createCommonApp() {
        return new MachineReassignmentApp();
    }
}
