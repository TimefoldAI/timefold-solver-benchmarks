package org.acme.facilitylocation.domain;

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
public class BenchmarkNeighborhoodProvider implements NeighborhoodProvider<FacilityPlan> {

    @Override
    public Neighborhood defineNeighborhood(NeighborhoodBuilder<FacilityPlan> builder) {
        var solution = builder.getSolutionMetaModel();
        var consumer = solution.genuineEntity(Consumer.class);
        builder.add(new ChangeMoveProvider<>(
                (PlanningVariableMetaModel<FacilityPlan, Consumer, Object>) consumer.variable("facility"), false));
        builder.add(new SwapMoveProvider<>(consumer));
        return builder.build();
    }
}
