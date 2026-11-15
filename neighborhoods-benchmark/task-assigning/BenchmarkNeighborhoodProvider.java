package org.acme.taskassigning.domain;

import ai.timefold.solver.core.preview.api.domain.metamodel.PlanningListVariableMetaModel;
import ai.timefold.solver.core.preview.api.move.builtin.ListChangeMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.ListSwapMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.TwoOptListMoveProvider;
import ai.timefold.solver.core.preview.api.neighborhood.Neighborhood;
import ai.timefold.solver.core.preview.api.neighborhood.NeighborhoodBuilder;
import ai.timefold.solver.core.preview.api.neighborhood.NeighborhoodProvider;

/**
 * Same moves, same order and same weights as selectors-solverConfig.xml.
 * crossingNull matches what the selectors include for the variable.
 */
@SuppressWarnings("unchecked")
public class BenchmarkNeighborhoodProvider implements NeighborhoodProvider<TaskAssigningSolution> {

    @Override
    public Neighborhood defineNeighborhood(NeighborhoodBuilder<TaskAssigningSolution> builder) {
        var solution = builder.getSolutionMetaModel();
        var employee = solution.genuineEntity(Employee.class);
        var employeeTasks = (PlanningListVariableMetaModel<TaskAssigningSolution, Employee, Object>) employee.variable("tasks");
        builder.add(new ListChangeMoveProvider<>(employeeTasks, true));
        builder.add(new ListSwapMoveProvider<>(employeeTasks));
        builder.add(new TwoOptListMoveProvider<>(employeeTasks, true));
        return builder.build();
    }
}
