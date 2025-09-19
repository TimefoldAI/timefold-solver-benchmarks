package ai.timefold.solver.benchmarks.examples.tsp.optional.score;

import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;
import ai.timefold.solver.core.api.score.buildin.simplelong.SimpleLongScore;
import ai.timefold.solver.core.api.score.calculator.EasyScoreCalculator;

public class TspEasyScoreCalculator implements EasyScoreCalculator<TspSolution, SimpleLongScore> {

    @Override
    public SimpleLongScore calculateScore(TspSolution tspSolution) {
        var visitList = tspSolution.getVisitList();
        long score = -(tspSolution.getDomicile().getDistanceToNextStandstill());
        for (var visit : visitList) {
            score -= visit.getDistanceToNextStandstill();
        }
        return SimpleLongScore.of(score);
    }

}
