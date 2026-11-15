package org.acme.bedallocation.domain;

import ai.timefold.solver.core.preview.api.domain.metamodel.PlanningVariableMetaModel;
import ai.timefold.solver.core.preview.api.move.builtin.ChangeMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.SwapMoveProvider;
import ai.timefold.solver.core.preview.api.neighborhood.Neighborhood;
import ai.timefold.solver.core.preview.api.neighborhood.NeighborhoodBuilder;
import ai.timefold.solver.core.preview.api.neighborhood.NeighborhoodProvider;

/**
 * Same moves, same order and same weights as selectors-solverConfig.xml.
 * crossingNull matches what the selectors include for the variable.
 */
@SuppressWarnings("unchecked")
public class BenchmarkNeighborhoodProvider implements NeighborhoodProvider<BedPlan> {

    @Override
    public Neighborhood defineNeighborhood(NeighborhoodBuilder<BedPlan> builder) {
        var solution = builder.getSolutionMetaModel();
        var stay = solution.genuineEntity(Stay.class);
        builder.add(new ChangeMoveProvider<>(
                (PlanningVariableMetaModel<BedPlan, Stay, Object>) stay.variable("bed"), true));
        builder.add(new SwapMoveProvider<>(stay));
        return builder.build();
    }
}
