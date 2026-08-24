package ai.timefold.solver.benchmarks.micro.moveprovider;

import ai.timefold.solver.benchmarks.examples.pas.domain.BedDesignation;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

/**
 * See {@link NoOpMeetingSchedulingConstraintProvider} - the move-provider benchmark never reads
 * score, so the real {@code PatientAdmissionScheduleConstraintProvider}'s joins are pure overhead
 * here.
 */
public final class NoOpPatientAdmissionScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                constraintFactory.forEach(BedDesignation.class)
                        .filter(bedDesignation -> false)
                        .penalize(HardMediumSoftScore.ONE_SOFT)
                        .asConstraint("No-op")
        };
    }

}
