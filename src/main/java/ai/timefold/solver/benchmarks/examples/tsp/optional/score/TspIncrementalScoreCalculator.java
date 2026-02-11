package ai.timefold.solver.benchmarks.examples.tsp.optional.score;

import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;
import ai.timefold.solver.benchmarks.examples.tsp.domain.Visit;
import ai.timefold.solver.core.api.score.buildin.simplelong.SimpleLongScore;
import ai.timefold.solver.core.api.score.calculator.IncrementalScoreCalculator;

public class TspIncrementalScoreCalculator implements IncrementalScoreCalculator<TspSolution, SimpleLongScore> {

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
    public void beforeEntityAdded(Object entity) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(Object entity) {
        insert((Visit) entity);
    }

    @Override
    public void beforeVariableChanged(Object entity, String variableName) {
        retract((Visit) entity);
    }

    @Override
    public void afterVariableChanged(Object entity, String variableName) {
        insert((Visit) entity);
    }

    @Override
    public void beforeEntityRemoved(Object entity) {
        retract((Visit) entity);
    }

    @Override
    public void afterEntityRemoved(Object entity) {
        // Do nothing
    }

    private void insert(Visit visit) {
        score -= visit.getDistanceFromPreviousVisit();
    }

    private void retract(Visit visit) {
        score += visit.getDistanceFromPreviousVisit();
    }

    @Override
    public SimpleLongScore calculateScore() {
        return SimpleLongScore.of(score);
    }

}
