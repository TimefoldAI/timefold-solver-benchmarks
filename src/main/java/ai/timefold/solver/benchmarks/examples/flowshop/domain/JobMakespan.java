package ai.timefold.solver.benchmarks.examples.flowshop.domain;

import java.util.Arrays;

public class JobMakespan {

    private final int[] makespanPerMachine;

    public JobMakespan(int numberOfMachines) {
        this.makespanPerMachine = new int[numberOfMachines];
    }

    public void setMakespan(int machineId, int value) {
        makespanPerMachine[machineId] = value;
    }

    public int getMakespan(int machineId) {
        return makespanPerMachine[machineId];
    }

    public int getLastMachineMakespan() {
        return makespanPerMachine[makespanPerMachine.length - 1];
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JobMakespan makespan)) {
            return false;
        }
        return Arrays.equals(makespanPerMachine, makespan.makespanPerMachine);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(makespanPerMachine);
    }
}
