package ai.timefold.solver.benchmarks.examples.tsp.optional.score;

import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;
import ai.timefold.solver.core.api.score.buildin.simplelong.SimpleLongScore;
import ai.timefold.solver.core.api.score.calculator.EasyScoreCalculator;

public class TspEasyScoreCalculator implements EasyScoreCalculator<TspSolution, SimpleLongScore> {

    @Override
    public SimpleLongScore calculateScore(TspSolution tspSolution) {
        var visitList = tspSolution.getVisitList();
        long score = 0;
        for (var visit : visitList) {
            score -= visit.getDistanceFromPreviousVisit();
        }
        score -= visitList.getLast().getDistanceToDepot();
        return SimpleLongScore.of(score);
    }

}
