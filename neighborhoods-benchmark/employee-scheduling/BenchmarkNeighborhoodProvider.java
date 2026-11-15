package org.acme.employeescheduling.domain;

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
public class BenchmarkNeighborhoodProvider implements NeighborhoodProvider<EmployeeSchedule> {

    @Override
    public Neighborhood defineNeighborhood(NeighborhoodBuilder<EmployeeSchedule> builder) {
        var solution = builder.getSolutionMetaModel();
        var shift = solution.genuineEntity(Shift.class);
        builder.add(new ChangeMoveProvider<>(
                (PlanningVariableMetaModel<EmployeeSchedule, Shift, Object>) shift.variable("employee"), false));
        builder.add(new SwapMoveProvider<>(shift));
        return builder.build();
    }
}
