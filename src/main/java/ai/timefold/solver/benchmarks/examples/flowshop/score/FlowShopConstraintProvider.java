package ai.timefold.solver.benchmarks.examples.flowshop.score;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.max;

import ai.timefold.solver.benchmarks.examples.flowshop.domain.Job;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

public class FlowShopConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                makespan(factory),
        };
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    protected Constraint makespan(ConstraintFactory factory) {
        return factory.forEach(Job.class)
                .groupBy(max(Job::getJobEndTime))
                .penalizeLong(HardSoftLongScore.ONE_SOFT, end -> end)
                .asConstraint("makespan");
    }
}
