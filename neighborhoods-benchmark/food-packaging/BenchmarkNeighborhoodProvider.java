package org.acme.foodpackaging.domain;

import ai.timefold.solver.core.preview.api.domain.metamodel.PlanningListVariableMetaModel;
import ai.timefold.solver.core.preview.api.domain.metamodel.PlanningVariableMetaModel;
import ai.timefold.solver.core.preview.api.move.builtin.ChangeMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.ListChangeMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.ListSwapMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.SwapMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.TwoOptListMoveProvider;
import ai.timefold.solver.core.preview.api.neighborhood.Neighborhood;
import ai.timefold.solver.core.preview.api.neighborhood.NeighborhoodBuilder;
import ai.timefold.solver.core.preview.api.neighborhood.NeighborhoodProvider;

/**
 * Same moves, same order and same weights as selectors-solverConfig.xml.
 * crossingNull matches what the selectors include for the variable.
 */
@SuppressWarnings("unchecked")
public class BenchmarkNeighborhoodProvider implements NeighborhoodProvider<PackagingSchedule> {

    @Override
    public Neighborhood defineNeighborhood(NeighborhoodBuilder<PackagingSchedule> builder) {
        var solution = builder.getSolutionMetaModel();
        var line = solution.genuineEntity(Line.class);
        builder.add(new ChangeMoveProvider<>(
                (PlanningVariableMetaModel<PackagingSchedule, Line, Object>) line.variable("operator"), false));
        builder.add(new SwapMoveProvider<>(line));
        var lineJobs = (PlanningListVariableMetaModel<PackagingSchedule, Line, Object>) line.variable("jobs");
        builder.add(new ListChangeMoveProvider<>(lineJobs, true));
        builder.add(new ListSwapMoveProvider<>(lineJobs));
        builder.add(new TwoOptListMoveProvider<>(lineJobs, true));
        return builder.build();
    }
}
