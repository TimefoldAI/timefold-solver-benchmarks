package ai.timefold.solver.benchmarks.examples.tsp.score;

import ai.timefold.solver.benchmarks.examples.tsp.domain.Standstill;
import ai.timefold.solver.core.api.score.buildin.simplelong.SimpleLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

public class TspConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                distanceToNextStandstill(constraintFactory)
        };
    }

    private Constraint distanceToNextStandstill(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Standstill.class)
                .penalizeLong(SimpleLongScore.ONE,
                        Standstill::getDistanceToNextStandstill)
                .asConstraint("Distance to next standstill");
    }

}
