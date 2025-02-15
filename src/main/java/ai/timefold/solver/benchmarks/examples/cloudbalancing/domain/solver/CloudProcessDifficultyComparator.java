package ai.timefold.solver.benchmarks.examples.cloudbalancing.domain.solver;

import java.util.Comparator;

import ai.timefold.solver.benchmarks.examples.cloudbalancing.domain.CloudProcess;

public class CloudProcessDifficultyComparator implements Comparator<CloudProcess> {

    private static final Comparator<CloudProcess> COMPARATOR = Comparator.comparingInt(CloudProcess::getRequiredMultiplicand)
            .thenComparingLong(CloudProcess::getId);

    @Override
    public int compare(CloudProcess a, CloudProcess b) {
        return COMPARATOR.compare(a, b);
    }

}
