package ai.timefold.solver.benchmarks.examples.tsp.optional.score;

import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;
import ai.timefold.solver.core.api.score.SimpleScore;
import ai.timefold.solver.core.api.score.calculator.EasyScoreCalculator;

public class TspEasyScoreCalculator implements EasyScoreCalculator<TspSolution, SimpleScore> {

    @Override
    public SimpleScore calculateScore(TspSolution tspSolution) {
        var visitList = tspSolution.getTour().getVisitList();
        long score = 0;
        for (var visit : visitList) {
            if (visit.getTour() != null) {
                score -= visit.getDistanceFromPreviousVisit();
                if (visit.getNext() == null) {
                    score -= visit.getDistanceToDepot();
                }
            }
        }
        return SimpleScore.of(score);
    }

}
