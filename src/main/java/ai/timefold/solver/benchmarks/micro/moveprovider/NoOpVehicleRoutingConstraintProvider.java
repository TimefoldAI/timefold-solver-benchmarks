package ai.timefold.solver.benchmarks.micro.moveprovider;

import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Customer;
import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

/**
 * See {@link NoOpMeetingSchedulingConstraintProvider} - the move-provider benchmark never reads
 * score, so the real {@code VehicleRoutingConstraintProvider}'s joins are pure overhead here.
 */
public final class NoOpVehicleRoutingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                constraintFactory.forEach(Customer.class)
                        .filter(customer -> false)
                        .penalize(HardSoftScore.ONE_SOFT)
                        .asConstraint("No-op")
        };
    }

}
