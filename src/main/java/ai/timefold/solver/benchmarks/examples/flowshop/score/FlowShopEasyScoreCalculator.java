package ai.timefold.solver.benchmarks.examples.flowshop.score;

import ai.timefold.solver.benchmarks.examples.flowshop.domain.JobScheduleSolution;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.calculator.EasyScoreCalculator;

import org.jspecify.annotations.NonNull;

public class FlowShopEasyScoreCalculator implements EasyScoreCalculator<JobScheduleSolution, HardSoftLongScore> {

    @Override
    public @NonNull HardSoftLongScore calculateScore(@NonNull JobScheduleSolution solution) {
        return HardSoftLongScore.ofSoft(-solution.getMachine().getJobs().getLast().getJobEndTime());
    }
}
