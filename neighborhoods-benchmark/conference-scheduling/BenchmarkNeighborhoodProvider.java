package org.acme.conferencescheduling.domain;

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
public class BenchmarkNeighborhoodProvider implements NeighborhoodProvider<ConferenceSchedule> {

    @Override
    public Neighborhood defineNeighborhood(NeighborhoodBuilder<ConferenceSchedule> builder) {
        var solution = builder.getSolutionMetaModel();
        var talk = solution.genuineEntity(Talk.class);
        builder.add(new ChangeMoveProvider<>(
                (PlanningVariableMetaModel<ConferenceSchedule, Talk, Object>) talk.variable("timeslot"), false));
        builder.add(new ChangeMoveProvider<>(
                (PlanningVariableMetaModel<ConferenceSchedule, Talk, Object>) talk.variable("room"), false));
        builder.add(new SwapMoveProvider<>(talk));
        return builder.build();
    }
}
