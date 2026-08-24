package ai.timefold.solver.benchmarks.micro.moveprovider;

import ai.timefold.solver.benchmarks.examples.taskassigning.domain.Task;
import ai.timefold.solver.core.api.score.BendableScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

/**
 * See {@link NoOpMeetingSchedulingConstraintProvider} - the move-provider benchmark never reads
 * score, so the real {@code TaskAssigningConstraintProvider}'s joins are pure overhead here.
 */
public final class NoOpTaskAssigningConstraintProvider implements ConstraintProvider {

    private static final int BENDABLE_SCORE_HARD_LEVELS_SIZE = 1;
    private static final int BENDABLE_SCORE_SOFT_LEVELS_SIZE = 5;

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                constraintFactory.forEach(Task.class)
                        .filter(task -> false)
                        .penalize(BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 0, 1))
                        .asConstraint("No-op")
        };
    }

}
