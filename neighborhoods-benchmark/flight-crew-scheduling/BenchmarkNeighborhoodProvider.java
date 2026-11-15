package org.acme.flightcrewscheduling.domain;

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
public class BenchmarkNeighborhoodProvider implements NeighborhoodProvider<FlightCrewSchedule> {

    @Override
    public Neighborhood defineNeighborhood(NeighborhoodBuilder<FlightCrewSchedule> builder) {
        var solution = builder.getSolutionMetaModel();
        var flightAssignment = solution.genuineEntity(FlightAssignment.class);
        builder.add(new ChangeMoveProvider<>(
                (PlanningVariableMetaModel<FlightCrewSchedule, FlightAssignment, Object>) flightAssignment.variable("employee"), false));
        builder.add(new SwapMoveProvider<>(flightAssignment));
        return builder.build();
    }
}
