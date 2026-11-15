package org.acme.maintenancescheduling.domain;

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
public class BenchmarkNeighborhoodProvider implements NeighborhoodProvider<MaintenanceSchedule> {

    @Override
    public Neighborhood defineNeighborhood(NeighborhoodBuilder<MaintenanceSchedule> builder) {
        var solution = builder.getSolutionMetaModel();
        var job = solution.genuineEntity(Job.class);
        builder.add(new ChangeMoveProvider<>(
                (PlanningVariableMetaModel<MaintenanceSchedule, Job, Object>) job.variable("crew"), false));
        builder.add(new ChangeMoveProvider<>(
                (PlanningVariableMetaModel<MaintenanceSchedule, Job, Object>) job.variable("startDate"), false));
        builder.add(new SwapMoveProvider<>(job));
        return builder.build();
    }
}
