package org.acme.orderpicking.domain;

import ai.timefold.solver.core.preview.api.domain.metamodel.PlanningListVariableMetaModel;
import ai.timefold.solver.core.preview.api.move.builtin.ListChangeMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.ListSwapMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.TwoOptListMoveProvider;
import ai.timefold.solver.core.preview.api.neighborhood.Neighborhood;
import ai.timefold.solver.core.preview.api.neighborhood.NeighborhoodBuilder;
import ai.timefold.solver.core.preview.api.neighborhood.NeighborhoodProvider;

/**
 * Same moves, same order and same weights as selectors-solverConfig.xml.
 * crossingNull matches what the selectors include for the variable.
 */
@SuppressWarnings("unchecked")
public class BenchmarkNeighborhoodProvider implements NeighborhoodProvider<PickingPlan> {

    @Override
    public Neighborhood defineNeighborhood(NeighborhoodBuilder<PickingPlan> builder) {
        var solution = builder.getSolutionMetaModel();
        var pickingCart = solution.genuineEntity(PickingCart.class);
        var pickingCartPickTasks = (PlanningListVariableMetaModel<PickingPlan, PickingCart, Object>) pickingCart.variable("pickTasks");
        builder.add(new ListChangeMoveProvider<>(pickingCartPickTasks, false));
        builder.add(new ListSwapMoveProvider<>(pickingCartPickTasks));
        builder.add(new TwoOptListMoveProvider<>(pickingCartPickTasks, true));
        return builder.build();
    }
}
