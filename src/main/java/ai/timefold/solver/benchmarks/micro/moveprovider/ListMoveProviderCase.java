package ai.timefold.solver.benchmarks.micro.moveprovider;

import java.util.function.Function;

import ai.timefold.solver.benchmarks.examples.taskassigning.domain.Employee;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Vehicle;
import ai.timefold.solver.benchmarks.micro.moveprovider.jmh.AbstractMoveProviderBenchmark;
import ai.timefold.solver.core.preview.api.domain.metamodel.PlanningSolutionMetaModel;
import ai.timefold.solver.core.preview.api.move.builtin.ListAssignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.ListChangeMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.ListSwapMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.ListTailSwapMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.ListUnassignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.MassListAssignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.MassListChangeMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.MassListUnassignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.SubListChangeMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.SubListSwapMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.SubListUnassignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.TwoOptListMoveProvider;
import ai.timefold.solver.core.preview.api.neighborhood.MoveProvider;
import ai.timefold.solver.core.preview.api.neighborhood.stream.dataset.sample.Samplers;

/**
 * The 12 built-in list-variable move providers, per
 * {@code /home/agent/.claude/plans/zany-drifting-ocean.md}. {@code SUB_LIST_UNASSIGN} uses a
 * task-assigning-specific bound ({@code MIN_SAMPLE_SIZE}..{@code TASK_ASSIGNING_MAX_SUB_LIST_SIZE})
 * rather than the shared {@code MAX_SAMPLE_SIZE}: task lists run 14-39 long, comfortably above 15,
 * so the bound draws noticeably larger sub-lists than its {@link #SUB_LIST_CHANGE} /
 * {@link #SUB_LIST_SWAP} counterparts on vehicle routing.
 */
public enum ListMoveProviderCase implements MoveProviderCase {

    LIST_CHANGE(Example.VEHICLE_ROUTING, metaModel -> {
        var entity = metaModel.genuineEntity(Vehicle.class);
        var customers = entity.listVariable("customers");
        return new ListChangeMoveProvider<>(customers);
    }),
    LIST_SWAP(Example.VEHICLE_ROUTING, metaModel -> {
        var entity = metaModel.genuineEntity(Vehicle.class);
        var customers = entity.listVariable("customers");
        return new ListSwapMoveProvider<>(customers);
    }),
    LIST_TAIL_SWAP(Example.VEHICLE_ROUTING, metaModel -> {
        var entity = metaModel.genuineEntity(Vehicle.class);
        var customers = entity.listVariable("customers");
        return new ListTailSwapMoveProvider<>(customers);
    }),
    TWO_OPT_LIST(Example.VEHICLE_ROUTING, metaModel -> {
        var entity = metaModel.genuineEntity(Vehicle.class);
        var customers = entity.listVariable("customers");
        return new TwoOptListMoveProvider<>(customers);
    }),
    MASS_LIST_CHANGE(Example.VEHICLE_ROUTING, metaModel -> {
        var entity = metaModel.genuineEntity(Vehicle.class);
        var customers = entity.listVariable("customers");
        return new MassListChangeMoveProvider<>(customers,
                Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE, AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE));
    }),
    SUB_LIST_CHANGE(Example.VEHICLE_ROUTING, metaModel -> {
        var entity = metaModel.genuineEntity(Vehicle.class);
        var customers = entity.listVariable("customers");
        return new SubListChangeMoveProvider<>(customers, AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE,
                AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE);
    }),
    SUB_LIST_SWAP(Example.VEHICLE_ROUTING, metaModel -> {
        var entity = metaModel.genuineEntity(Vehicle.class);
        var customers = entity.listVariable("customers");
        return new SubListSwapMoveProvider<>(customers, AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE,
                AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE);
    }),
    LIST_ASSIGN(Example.TASK_ASSIGNING, metaModel -> {
        var entity = metaModel.genuineEntity(Employee.class);
        var tasks = entity.listVariable("tasks");
        return new ListAssignMoveProvider<>(tasks);
    }),
    LIST_UNASSIGN(Example.TASK_ASSIGNING, metaModel -> {
        var entity = metaModel.genuineEntity(Employee.class);
        var tasks = entity.listVariable("tasks");
        return new ListUnassignMoveProvider<>(tasks);
    }),
    MASS_LIST_ASSIGN(Example.TASK_ASSIGNING, metaModel -> {
        var entity = metaModel.genuineEntity(Employee.class);
        var tasks = entity.listVariable("tasks");
        return new MassListAssignMoveProvider<>(tasks,
                Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE, AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE));
    }),
    MASS_LIST_UNASSIGN(Example.TASK_ASSIGNING, metaModel -> {
        var entity = metaModel.genuineEntity(Employee.class);
        var tasks = entity.listVariable("tasks");
        return new MassListUnassignMoveProvider<>(tasks,
                Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE, AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE));
    }),
    SUB_LIST_UNASSIGN(Example.TASK_ASSIGNING, metaModel -> {
        var entity = metaModel.genuineEntity(Employee.class);
        var tasks = entity.listVariable("tasks");
        return new SubListUnassignMoveProvider<>(tasks, AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE,
                AbstractMoveProviderBenchmark.TASK_ASSIGNING_MAX_SUB_LIST_SIZE);
    });

    private final Example example;
    private final Function<PlanningSolutionMetaModel<?>, MoveProvider<?>> providerFactory;

    ListMoveProviderCase(Example example, Function<PlanningSolutionMetaModel<?>, MoveProvider<?>> providerFactory) {
        this.example = example;
        this.providerFactory = providerFactory;
    }

    @Override
    public Example getExample() {
        return example;
    }

    @Override
    public MoveProvider<?> createMoveProvider(PlanningSolutionMetaModel<?> solutionMetaModel) {
        return providerFactory.apply(solutionMetaModel);
    }

}
