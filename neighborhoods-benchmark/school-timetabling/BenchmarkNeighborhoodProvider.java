package org.acme.schooltimetabling.domain;

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
public class BenchmarkNeighborhoodProvider implements NeighborhoodProvider<Timetable> {

    @Override
    public Neighborhood defineNeighborhood(NeighborhoodBuilder<Timetable> builder) {
        var solution = builder.getSolutionMetaModel();
        var lesson = solution.genuineEntity(Lesson.class);
        builder.add(new ChangeMoveProvider<>(
                (PlanningVariableMetaModel<Timetable, Lesson, Object>) lesson.variable("timeslot"), false));
        builder.add(new ChangeMoveProvider<>(
                (PlanningVariableMetaModel<Timetable, Lesson, Object>) lesson.variable("room"), false));
        builder.add(new SwapMoveProvider<>(lesson));
        return builder.build();
    }
}
