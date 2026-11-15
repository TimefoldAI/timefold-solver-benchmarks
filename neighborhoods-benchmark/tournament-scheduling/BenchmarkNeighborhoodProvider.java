package org.acme.tournamentschedule.domain;

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
public class BenchmarkNeighborhoodProvider implements NeighborhoodProvider<TournamentSchedule> {

    @Override
    public Neighborhood defineNeighborhood(NeighborhoodBuilder<TournamentSchedule> builder) {
        var solution = builder.getSolutionMetaModel();
        var teamAssignment = solution.genuineEntity(TeamAssignment.class);
        builder.add(new ChangeMoveProvider<>(
                (PlanningVariableMetaModel<TournamentSchedule, TeamAssignment, Object>) teamAssignment.variable("team"), false));
        builder.add(new SwapMoveProvider<>(teamAssignment));
        return builder.build();
    }
}
