package ai.timefold.solver.benchmarks.examples.tsp.optional.score;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ai.timefold.solver.benchmarks.examples.tsp.domain.Domicile;
import ai.timefold.solver.benchmarks.examples.tsp.domain.Standstill;
import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;
import ai.timefold.solver.benchmarks.examples.tsp.domain.Visit;
import ai.timefold.solver.core.api.score.buildin.simplelong.SimpleLongScore;
import ai.timefold.solver.core.api.score.calculator.EasyScoreCalculator;

public class TspEasyScoreCalculator implements EasyScoreCalculator<TspSolution, SimpleLongScore> {

    @Override
    public SimpleLongScore calculateScore(TspSolution tspSolution) {
        List<Visit> visitList = tspSolution.getVisitList();
        Set<Visit> tailVisitSet = new HashSet<>(visitList);
        long score = 0L;
        for (Visit visit : visitList) {
            Standstill previousStandstill = visit.getPreviousStandstill();
            if (previousStandstill != null) {
                score -= visit.getDistanceFromPreviousStandstill();
                if (previousStandstill instanceof Visit) {
                    tailVisitSet.remove(previousStandstill);
                }
            }
        }
        Domicile domicile = tspSolution.getDomicile();
        for (Visit tailVisit : tailVisitSet) {
            if (tailVisit.getPreviousStandstill() != null) {
                score -= tailVisit.getDistanceTo(domicile);
            }
        }
        return SimpleLongScore.of(score);
    }

}
