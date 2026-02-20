package ai.timefold.solver.benchmarks.examples.tsp.score;

import ai.timefold.solver.benchmarks.examples.tsp.domain.Visit;
import ai.timefold.solver.core.api.score.SimpleScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

public class TspConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                distanceToPreviousStandstillPossiblyWithReturnToDepot(constraintFactory)
        };
    }

    protected Constraint distanceToPreviousStandstillPossiblyWithReturnToDepot(ConstraintFactory factory) {
        return factory.forEach(Visit.class)
                .filter(visit -> visit.getTour() != null)
                .penalize(SimpleScore.ONE, visit -> {
                    var distance = visit.getDistanceFromPreviousVisit();
                    if (visit.getNext() == null) {
                        distance += visit.getDistanceToDepot();
                    }
                    return distance;
                }).asConstraint("distanceToPreviousStandstillPossiblyWithReturnToDepot");
    }

}
