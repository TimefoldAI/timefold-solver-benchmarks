package ai.timefold.solver.benchmarks.micro.moveprovider;

import java.util.function.Function;
import java.util.function.ToIntFunction;

import ai.timefold.solver.benchmarks.examples.taskassigning.domain.Employee;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Vehicle;
import ai.timefold.solver.benchmarks.micro.moveprovider.jmh.AbstractMoveProviderBenchmark;
import ai.timefold.solver.core.preview.api.domain.metamodel.PlanningSolutionMetaModel;
import ai.timefold.solver.core.preview.api.move.Move;
import ai.timefold.solver.core.preview.api.move.builtin.ListAssignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.ListChangeMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.ListSwapMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.ListTailSwapMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.ListUnassignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.MassListAssignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.MassListChangeMove;
import ai.timefold.solver.core.preview.api.move.builtin.MassListChangeMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.MassListUnassignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.SubListChangeMove;
import ai.timefold.solver.core.preview.api.move.builtin.SubListChangeMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.SubListSwapMove;
import ai.timefold.solver.core.preview.api.move.builtin.SubListSwapMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.SubListUnassignMove;
import ai.timefold.solver.core.preview.api.move.builtin.SubListUnassignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.TwoOptListMoveProvider;
import ai.timefold.solver.core.preview.api.neighborhood.MoveProvider;
import ai.timefold.solver.core.preview.api.neighborhood.stream.dataset.sample.Samplers;

/**
 * The 12 built-in list-variable move providers. {@code SUB_LIST_UNASSIGN} uses a
 * task-assigning-specific bound ({@code MIN_SAMPLE_SIZE}..{@code TASK_ASSIGNING_MAX_SUB_LIST_SIZE})
 * rather than the shared {@code MAX_SAMPLE_SIZE}: task lists run 14-39 long, comfortably above 15,
 * so the bound draws noticeably larger sub-lists than its {@link #SUB_LIST_CHANGE} /
 * {@link #SUB_LIST_SWAP} counterparts on vehicle routing.
 *
 * <p>
 * Every case also carries a {@code movedValueCounter}, the quantity of work in one of its moves, in
 * variable writes; see {@link MoveProviderCase#countMovedValues(Move)}. A list variable is a single
 * variable, so the count is simply the number of values the move moves. Two counts surprise a
 * reader. A swap counts both sides, so {@link #LIST_SWAP} reads as 2, not 1. And every count taken
 * from a {@code Range} - the sub-list, tail-swap and two-opt cases - grows with the list length, so
 * it is not bounded by {@code MAX_SAMPLE_SIZE}.
 */
public enum ListMoveProviderCase implements MoveProviderCase {

    LIST_CHANGE(Example.VEHICLE_ROUTING, metaModel -> {
        var entity = metaModel.genuineEntity(Vehicle.class);
        var customers = entity.listVariable("customers");
        // Emits ListChangeMove, ListAssignMove or ListUnassignMove; all three move one value.
        return new ListChangeMoveProvider<>(customers);
    }, move -> 1),
    LIST_SWAP(Example.VEHICLE_ROUTING, metaModel -> {
        var entity = metaModel.genuineEntity(Vehicle.class);
        var customers = entity.listVariable("customers");
        // Emits ListSwapMove, or a CompositeMove of an unassign plus an assign; both move two values.
        return new ListSwapMoveProvider<>(customers);
    }, move -> 2),
    LIST_TAIL_SWAP(Example.VEHICLE_ROUTING, metaModel -> {
        var entity = metaModel.genuineEntity(Vehicle.class);
        var customers = entity.listVariable("customers");
        return new ListTailSwapMoveProvider<>(customers);
    }, ListMoveProviderCase::countSubListSwap),
    TWO_OPT_LIST(Example.VEHICLE_ROUTING, metaModel -> {
        var entity = metaModel.genuineEntity(Vehicle.class);
        var customers = entity.listVariable("customers");
        return new TwoOptListMoveProvider<>(customers);
    }, ListMoveProviderCase::countTwoOptList),
    MASS_LIST_CHANGE(Example.VEHICLE_ROUTING, metaModel -> {
        var entity = metaModel.genuineEntity(Vehicle.class);
        var customers = entity.listVariable("customers");
        return new MassListChangeMoveProvider<>(customers,
                Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE, AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE));
    }, ListMoveProviderCase::countMassListChange),
    SUB_LIST_CHANGE(Example.VEHICLE_ROUTING, metaModel -> {
        var entity = metaModel.genuineEntity(Vehicle.class);
        var customers = entity.listVariable("customers");
        return new SubListChangeMoveProvider<>(customers, AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE,
                AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE);
    }, ListMoveProviderCase::countSubListChange),
    SUB_LIST_SWAP(Example.VEHICLE_ROUTING, metaModel -> {
        var entity = metaModel.genuineEntity(Vehicle.class);
        var customers = entity.listVariable("customers");
        return new SubListSwapMoveProvider<>(customers, AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE,
                AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE);
    }, ListMoveProviderCase::countSubListSwap),
    LIST_ASSIGN(Example.TASK_ASSIGNING, metaModel -> {
        var entity = metaModel.genuineEntity(Employee.class);
        var tasks = entity.listVariable("tasks");
        return new ListAssignMoveProvider<>(tasks);
    }, move -> 1),
    LIST_UNASSIGN(Example.TASK_ASSIGNING, metaModel -> {
        var entity = metaModel.genuineEntity(Employee.class);
        var tasks = entity.listVariable("tasks");
        return new ListUnassignMoveProvider<>(tasks);
    }, move -> 1),
    MASS_LIST_ASSIGN(Example.TASK_ASSIGNING, metaModel -> {
        var entity = metaModel.genuineEntity(Employee.class);
        var tasks = entity.listVariable("tasks");
        return new MassListAssignMoveProvider<>(tasks,
                Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE, AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE));
    }, ListMoveProviderCase::countMassListChange),
    MASS_LIST_UNASSIGN(Example.TASK_ASSIGNING, metaModel -> {
        var entity = metaModel.genuineEntity(Employee.class);
        var tasks = entity.listVariable("tasks");
        return new MassListUnassignMoveProvider<>(tasks,
                Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE, AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE));
    }, ListMoveProviderCase::countMassListChange),
    SUB_LIST_UNASSIGN(Example.TASK_ASSIGNING, metaModel -> {
        var entity = metaModel.genuineEntity(Employee.class);
        var tasks = entity.listVariable("tasks");
        return new SubListUnassignMoveProvider<>(tasks, AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE,
                AbstractMoveProviderBenchmark.TASK_ASSIGNING_MAX_SUB_LIST_SIZE);
    }, move -> ((SubListUnassignMove<?, ?, ?>) move).getRange().length());

    /**
     * Both the mass list change and the mass list assign and unassign providers emit
     * {@link MassListChangeMove}; the assign and unassign variants only differ in the destination.
     */
    private static int countMassListChange(Move<?> move) {
        return ((MassListChangeMove<?, ?, ?>) move).getSample().size();
    }

    private static int countSubListSwap(Move<?> move) {
        var subListSwapMove = (SubListSwapMove<?, ?, ?>) move;
        return subListSwapMove.getLeftRange().length() + subListSwapMove.getRightRange().length();
    }

    /**
     * {@link SubListChangeMoveProvider} unassigns the sub list when the destination is the unassigned
     * position, and emits {@link SubListUnassignMove} for that.
     */
    private static int countSubListChange(Move<?> move) {
        return move instanceof SubListChangeMove<?, ?, ?> subListChangeMove
                ? subListChangeMove.getSource().length()
                : ((SubListUnassignMove<?, ?, ?>) move).getRange().length();
    }

    /**
     * {@link TwoOptListMoveProvider} reverses a span inside one list, which is a
     * {@link SubListChangeMove}, and swaps two tails between two lists, which is a
     * {@link SubListSwapMove}.
     */
    private static int countTwoOptList(Move<?> move) {
        return move instanceof SubListChangeMove<?, ?, ?> subListChangeMove
                ? subListChangeMove.getSource().length()
                : countSubListSwap(move);
    }

    private final Example example;
    private final Function<PlanningSolutionMetaModel<?>, MoveProvider<?>> providerFactory;
    private final ToIntFunction<Move<?>> movedValueCounter;

    ListMoveProviderCase(Example example, Function<PlanningSolutionMetaModel<?>, MoveProvider<?>> providerFactory,
            ToIntFunction<Move<?>> movedValueCounter) {
        this.example = example;
        this.providerFactory = providerFactory;
        this.movedValueCounter = movedValueCounter;
    }

    @Override
    public Example getExample() {
        return example;
    }

    @Override
    public MoveProvider<?> createMoveProvider(PlanningSolutionMetaModel<?> solutionMetaModel) {
        return providerFactory.apply(solutionMetaModel);
    }

    @Override
    public int countMovedValues(Move<?> move) {
        return movedValueCounter.applyAsInt(move);
    }

}
