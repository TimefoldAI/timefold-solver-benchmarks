package org.acme.meetingschedule.domain;

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
public class BenchmarkNeighborhoodProvider implements NeighborhoodProvider<MeetingSchedule> {

    @Override
    public Neighborhood defineNeighborhood(NeighborhoodBuilder<MeetingSchedule> builder) {
        var solution = builder.getSolutionMetaModel();
        var meetingAssignment = solution.genuineEntity(MeetingAssignment.class);
        builder.add(new ChangeMoveProvider<>(
                (PlanningVariableMetaModel<MeetingSchedule, MeetingAssignment, Object>) meetingAssignment.variable("startingTimeGrain"), false));
        builder.add(new ChangeMoveProvider<>(
                (PlanningVariableMetaModel<MeetingSchedule, MeetingAssignment, Object>) meetingAssignment.variable("room"), false));
        builder.add(new SwapMoveProvider<>(meetingAssignment));
        return builder.build();
    }
}
