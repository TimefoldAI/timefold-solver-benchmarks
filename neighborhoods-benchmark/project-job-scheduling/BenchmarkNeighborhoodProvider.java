package org.acme.projectjobschedule.domain;

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
public class BenchmarkNeighborhoodProvider implements NeighborhoodProvider<ProjectJobSchedule> {

    @Override
    public Neighborhood defineNeighborhood(NeighborhoodBuilder<ProjectJobSchedule> builder) {
        var solution = builder.getSolutionMetaModel();
        var allocation = solution.genuineEntity(Allocation.class);
        builder.add(new ChangeMoveProvider<>(
                (PlanningVariableMetaModel<ProjectJobSchedule, Allocation, Object>) allocation.variable("executionMode"), false));
        builder.add(new ChangeMoveProvider<>(
                (PlanningVariableMetaModel<ProjectJobSchedule, Allocation, Object>) allocation.variable("delay"), false));
        builder.add(new SwapMoveProvider<>(allocation));
        return builder.build();
    }
}
