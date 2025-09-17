package ai.timefold.solver.benchmarks.examples.tsp.score;

import ai.timefold.solver.benchmarks.examples.tsp.domain.Domicile;
import ai.timefold.solver.benchmarks.examples.tsp.domain.Visit;
import ai.timefold.solver.core.api.score.buildin.simplelong.SimpleLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

public class TspConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                distanceToPreviousStandstill(constraintFactory),
                distanceFromLastVisitToDomicile(constraintFactory)
        };
    }

    private Constraint distanceToPreviousStandstill(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Visit.class)
                .penalizeLong(SimpleLongScore.ONE,
                        Visit::getDistanceFromPreviousStandstill)
                .asConstraint("Distance to previous standstill");
    }

    private Constraint distanceFromLastVisitToDomicile(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Visit.class)
                .filter(visit -> visit.getNextStandstill() == null)
                .join(Domicile.class)
                .penalizeLong(SimpleLongScore.ONE,
                        Visit::getDistanceTo)
                .asConstraint("Distance from last visit to domicile");
    }
}
