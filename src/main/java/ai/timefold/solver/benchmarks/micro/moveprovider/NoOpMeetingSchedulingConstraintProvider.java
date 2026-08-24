package ai.timefold.solver.benchmarks.micro.moveprovider;

import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.MeetingAssignment;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

/**
 * The move-provider benchmark never reads score (see {@code MoveProviderProblem}'s javadoc), so the
 * real {@code MeetingSchedulingConstraintProvider}'s {@code roomConflict} self-join - quadratic in
 * how many meetings overlap per room, and deliberately huge on
 * {@code Example#MEETING_SCHEDULING_DENSE} - was paying for join/score-impact allocation nobody
 * measures, at every iteration setup and every periodic flush. This constraint never matches, so the
 * constraint-stream network stays a single cheap filter node instead.
 */
public final class NoOpMeetingSchedulingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                constraintFactory.forEach(MeetingAssignment.class)
                        .filter(meetingAssignment -> false)
                        .penalize(HardMediumSoftScore.ONE_SOFT)
                        .asConstraint("No-op")
        };
    }

}
