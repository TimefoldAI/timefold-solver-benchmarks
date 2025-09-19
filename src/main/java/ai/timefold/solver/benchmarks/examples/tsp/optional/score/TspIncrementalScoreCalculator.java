package ai.timefold.solver.benchmarks.examples.tsp.optional.score;

import ai.timefold.solver.benchmarks.examples.tsp.domain.Standstill;
import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;
import ai.timefold.solver.benchmarks.examples.tsp.domain.Visit;
import ai.timefold.solver.core.api.score.buildin.simplelong.SimpleLongScore;
import ai.timefold.solver.core.api.score.calculator.IncrementalScoreCalculator;

public class TspIncrementalScoreCalculator implements IncrementalScoreCalculator<TspSolution, SimpleLongScore> {

    private long score;

    @Override
    public void resetWorkingSolution(TspSolution tspSolution) {
        score = 0L;
        insert(tspSolution.getDomicile());
        for (Visit visit : tspSolution.getVisitList()) {
            insert(visit);
        }
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
        retract((Standstill) entity);
    }

    @Override
    public void afterVariableChanged(Object entity, String variableName) {
        insert((Standstill) entity);
    }

    @Override
    public void beforeEntityRemoved(Object entity) {
        retract((Visit) entity);
    }

    @Override
    public void afterEntityRemoved(Object entity) {
        // Do nothing
    }

    private void insert(Standstill visit) {
        score -= visit.getDistanceToNextStandstill();
    }

    private void retract(Standstill visit) {
        score += visit.getDistanceToNextStandstill();
    }

    @Override
    public SimpleLongScore calculateScore() {
        return SimpleLongScore.of(score);
    }

}
