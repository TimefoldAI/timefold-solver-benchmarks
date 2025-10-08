package ai.timefold.solver.benchmarks.examples.flowshop.domain;

import java.util.Arrays;

public class JobMachineTime {

    private final int[] time;

    public JobMachineTime(int numberOfMachines) {
        this.time = new int[numberOfMachines];
    }

    public int setTime(int machineId, int value) {
        return time[machineId] = value;
    }

    public int getTime(int machineId) {
        return time[machineId];
    }

    public int getLastMachineTime() {
        return time[time.length - 1];
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JobMachineTime other)) {
            return false;
        }
        return Arrays.equals(time, other.time);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(time);
    }
}
