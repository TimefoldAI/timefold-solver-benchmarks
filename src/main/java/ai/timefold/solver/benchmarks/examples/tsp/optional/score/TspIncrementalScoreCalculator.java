package ai.timefold.solver.benchmarks.examples.tsp.optional.score;

import ai.timefold.solver.benchmarks.examples.tsp.domain.Tour;
import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;
import ai.timefold.solver.core.api.score.SimpleScore;
import ai.timefold.solver.core.api.score.calculator.IncrementalScoreCalculator;

import org.jspecify.annotations.NonNull;

public class TspIncrementalScoreCalculator implements IncrementalScoreCalculator<TspSolution, SimpleScore> {

    private long score;

    @Override
    public void resetWorkingSolution(TspSolution tspSolution) {
        score = 0L;
        for (var visit : tspSolution.getVisitList()) {
            score -= visit.getDistanceFromPreviousVisit();
        }
        score -= tspSolution.getVisitList().getLast().getDistanceToDepot();
    }

    @Override
    public void beforeEntityAdded(@NonNull Object o) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(@NonNull Object o) {
        // Do nothing
    }

    @Override
    public void beforeVariableChanged(@NonNull Object o, @NonNull String s) {
        // Do nothing
    }

    @Override
    public void afterVariableChanged(@NonNull Object o, @NonNull String s) {
        // Do nothing
    }

    @Override
    public void beforeListVariableChanged(@NonNull Object entity, @NonNull String variableName, int fromIndex, int toIndex) {
        var tour = (Tour) entity;
        for (int index = fromIndex; index < toIndex; index++) {
            var visit = tour.getVisitList().get(index);
            score += visit.getDistanceFromPreviousVisit();
        }
        if (toIndex < tour.getVisitList().size()) {
            score += tour.getVisitList().get(toIndex).getDistanceFromPreviousVisit();
        } else if (toIndex > 0 && tour.getVisitList().get(toIndex - 1).getNext() != null) {
            score += tour.getVisitList().get(toIndex - 1).getDistanceToDepot();
        }
    }

    @Override
    public void afterListVariableChanged(@NonNull Object entity, @NonNull String variableName, int fromIndex, int toIndex) {
        var tour = (Tour) entity;
        for (int index = fromIndex; index < toIndex; index++) {
            var visit = tour.getVisitList().get(index);
            score -= visit.getDistanceFromPreviousVisit();
        }
        if (toIndex < tour.getVisitList().size()) {
            score -= tour.getVisitList().get(toIndex).getDistanceFromPreviousVisit();
        } else if (toIndex > 0 && tour.getVisitList().get(toIndex - 1).getNext() != null) {
            score -= tour.getVisitList().get(toIndex - 1).getDistanceToDepot();
        }
    }

    @Override
    public void beforeEntityRemoved(@NonNull Object o) {
        // Do nothing
    }

    @Override
    public void afterEntityRemoved(@NonNull Object o) {
        // Do nothing
    }

    @Override
    public SimpleScore calculateScore() {
        return SimpleScore.of(score);
    }

}
