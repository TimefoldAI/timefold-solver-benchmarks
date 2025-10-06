package ai.timefold.solver.benchmarks.examples.flowshop.score;

import ai.timefold.solver.benchmarks.examples.flowshop.domain.Machine;
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
        return factory.forEach(Machine.class)
                .penalizeLong(HardSoftLongScore.ONE_SOFT, Machine::getMakespan)
                .asConstraint("makespan");
    }
}
