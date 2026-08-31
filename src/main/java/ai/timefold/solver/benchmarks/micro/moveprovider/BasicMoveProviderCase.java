package ai.timefold.solver.benchmarks.micro.moveprovider;

import java.util.function.Function;
import java.util.function.ToIntFunction;

import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.MeetingAssignment;
import ai.timefold.solver.benchmarks.examples.pas.domain.BedDesignation;
import ai.timefold.solver.benchmarks.micro.moveprovider.jmh.AbstractMoveProviderBenchmark;
import ai.timefold.solver.core.preview.api.domain.metamodel.PlanningSolutionMetaModel;
import ai.timefold.solver.core.preview.api.move.Move;
import ai.timefold.solver.core.preview.api.move.builtin.AssignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.ChangeMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.MassAssignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.MassChangeMove;
import ai.timefold.solver.core.preview.api.move.builtin.MassChangeMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.MassUnassignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.PillarChangeMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.PillarSwapMove;
import ai.timefold.solver.core.preview.api.move.builtin.PillarSwapMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.PillarUnassignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.SubPillarChangeMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.SubPillarSwapMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.SubPillarUnassignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.SwapMove;
import ai.timefold.solver.core.preview.api.move.builtin.SwapMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.UnassignMoveProvider;
import ai.timefold.solver.core.preview.api.neighborhood.MoveProvider;
import ai.timefold.solver.core.preview.api.neighborhood.stream.dataset.sample.Samplers;

/**
 * The 13 built-in basic-variable move providers. {@code PILLAR_CHANGE} and
 * {@code SUB_PILLAR_CHANGE} key on {@code room} rather than {@code startingTimeGrain}: 400 meetings
 * over 1280 grains means grain-keyed pillars are singletons, and {@link Samplers#between(int, int)}
 * drains then discards any slice below its minimum. {@code PILLAR_SWAP} and {@code SUB_PILLAR_SWAP}
 * run on {@link Example#MEETING_SCHEDULING_DENSE} instead, the only shape with non-singleton
 * composite (room, startingTimeGrain) pillars.
 *
 * <p>
 * Every case also carries a {@code movedValueCounter}, the quantity of work in one of its moves, in
 * variable writes; see {@link MoveProviderCase#countMovedValues(Move)}. Two counts surprise a
 * reader. A swap counts both sides and every variable, so a swap of one pair of two-variable
 * entities reads as 4, not 1. And a pillar swap's count is the size of both pillars together, times
 * the variable count, so it is far larger than the sampler bounds suggest.
 */
public enum BasicMoveProviderCase implements MoveProviderCase {

    CHANGE(Example.MEETING_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(MeetingAssignment.class);
        var grain = entity.basicVariable("startingTimeGrain");
        return new ChangeMoveProvider<>(grain);
    }, move -> 1),
    MASS_CHANGE(Example.MEETING_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(MeetingAssignment.class);
        var grain = entity.basicVariable("startingTimeGrain");
        return new MassChangeMoveProvider<>(grain,
                Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE, AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE));
    }, BasicMoveProviderCase::countMassChange),
    SWAP(Example.MEETING_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(MeetingAssignment.class);
        return new SwapMoveProvider<>(entity);
    }, move -> ((SwapMove<?, ?>) move).variableMetaModels().size() * 2),
    PILLAR_CHANGE(Example.MEETING_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(MeetingAssignment.class);
        var room = entity.basicVariable("room");
        return new PillarChangeMoveProvider<>(room);
    }, BasicMoveProviderCase::countMassChange),
    SUB_PILLAR_CHANGE(Example.MEETING_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(MeetingAssignment.class);
        var room = entity.basicVariable("room");
        return new SubPillarChangeMoveProvider<>(room, Samplers.pillar(
                Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE,
                        AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE)));
    }, BasicMoveProviderCase::countMassChange),
    PILLAR_SWAP(Example.MEETING_SCHEDULING_DENSE, metaModel -> {
        var entity = metaModel.genuineEntity(MeetingAssignment.class);
        return new PillarSwapMoveProvider<>(entity);
    }, BasicMoveProviderCase::countPillarSwap),
    SUB_PILLAR_SWAP(Example.MEETING_SCHEDULING_DENSE, metaModel -> {
        var entity = metaModel.genuineEntity(MeetingAssignment.class);
        return new SubPillarSwapMoveProvider<>(entity,
                Samplers.pillar(
                        Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE,
                                AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE)),
                Samplers.pillar(
                        Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE,
                                AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE)));
    }, BasicMoveProviderCase::countPillarSwap),
    ASSIGN(Example.PATIENT_ADMISSION_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(BedDesignation.class);
        var bed = entity.basicVariable("bed");
        return new AssignMoveProvider<>(bed);
    }, move -> 1),
    UNASSIGN(Example.PATIENT_ADMISSION_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(BedDesignation.class);
        var bed = entity.basicVariable("bed");
        return new UnassignMoveProvider<>(bed);
    }, move -> 1),
    MASS_ASSIGN(Example.PATIENT_ADMISSION_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(BedDesignation.class);
        var bed = entity.basicVariable("bed");
        return new MassAssignMoveProvider<>(bed,
                Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE, AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE));
    }, BasicMoveProviderCase::countMassChange),
    MASS_UNASSIGN(Example.PATIENT_ADMISSION_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(BedDesignation.class);
        var bed = entity.basicVariable("bed");
        return new MassUnassignMoveProvider<>(bed,
                Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE, AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE));
    }, BasicMoveProviderCase::countMassChange),
    PILLAR_UNASSIGN(Example.PATIENT_ADMISSION_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(BedDesignation.class);
        var bed = entity.basicVariable("bed");
        return new PillarUnassignMoveProvider<>(bed);
    }, BasicMoveProviderCase::countMassChange),
    SUB_PILLAR_UNASSIGN(Example.PATIENT_ADMISSION_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(BedDesignation.class);
        var bed = entity.basicVariable("bed");
        return new SubPillarUnassignMoveProvider<>(bed, Samplers.pillar(
                Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE,
                        AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE)));
    }, BasicMoveProviderCase::countMassChange);

    /**
     * Every mass, pillar and sub-pillar provider here emits {@link MassChangeMove}; there is no
     * dedicated pillar change move. The move holds a single variable meta model, so the sample size
     * is already the variable-write count.
     */
    private static int countMassChange(Move<?> move) {
        return ((MassChangeMove<?, ?, ?>) move).getSample().size();
    }

    private static int countPillarSwap(Move<?> move) {
        var pillarSwapMove = (PillarSwapMove<?, ?>) move;
        return pillarSwapMove.variableMetaModels().size()
                * (pillarSwapMove.getLeftPillar().size() + pillarSwapMove.getRightPillar().size());
    }

    private final Example example;
    private final Function<PlanningSolutionMetaModel<?>, MoveProvider<?>> providerFactory;
    private final ToIntFunction<Move<?>> movedValueCounter;

    BasicMoveProviderCase(Example example, Function<PlanningSolutionMetaModel<?>, MoveProvider<?>> providerFactory,
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
