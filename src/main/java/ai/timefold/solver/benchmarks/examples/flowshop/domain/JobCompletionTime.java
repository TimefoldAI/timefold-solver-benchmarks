package ai.timefold.solver.benchmarks.examples.flowshop.domain;

import java.util.Arrays;

public class JobCompletionTime {

    private final int[] completionTime;

    public JobCompletionTime(int numberOfMachines) {
        this.completionTime = new int[numberOfMachines];
    }

    public int setCompletionTime(int machineId, int value) {
        return completionTime[machineId] = value;
    }

    public int getCompletionTime(int machineId) {
        return completionTime[machineId];
    }

    public int getCompletionTimeLastMachine() {
        return completionTime[completionTime.length - 1];
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JobCompletionTime makespan)) {
            return false;
        }
        return Arrays.equals(completionTime, makespan.completionTime);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(completionTime);
    }
}
