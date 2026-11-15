package org.acme.sportsleagueschedule.domain;

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
public class BenchmarkNeighborhoodProvider implements NeighborhoodProvider<LeagueSchedule> {

    @Override
    public Neighborhood defineNeighborhood(NeighborhoodBuilder<LeagueSchedule> builder) {
        var solution = builder.getSolutionMetaModel();
        var match = solution.genuineEntity(Match.class);
        builder.add(new ChangeMoveProvider<>(
                (PlanningVariableMetaModel<LeagueSchedule, Match, Object>) match.variable("round"), false));
        builder.add(new SwapMoveProvider<>(match));
        return builder.build();
    }
}
