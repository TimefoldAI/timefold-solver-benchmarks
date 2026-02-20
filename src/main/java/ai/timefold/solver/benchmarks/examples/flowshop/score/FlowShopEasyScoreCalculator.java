package ai.timefold.solver.benchmarks.examples.flowshop.score;

import ai.timefold.solver.benchmarks.examples.flowshop.domain.JobScheduleSolution;
import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.calculator.EasyScoreCalculator;

import org.jspecify.annotations.NonNull;

public class FlowShopEasyScoreCalculator implements EasyScoreCalculator<JobScheduleSolution, HardSoftScore> {

    @Override
    public @NonNull HardSoftScore calculateScore(@NonNull JobScheduleSolution solution) {
        return HardSoftScore.ofSoft(-solution.getMachine().getMakespan());
    }
}
