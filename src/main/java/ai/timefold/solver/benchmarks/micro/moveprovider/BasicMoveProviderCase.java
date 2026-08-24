package ai.timefold.solver.benchmarks.micro.moveprovider;

import java.util.function.Function;

import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.MeetingAssignment;
import ai.timefold.solver.benchmarks.examples.pas.domain.BedDesignation;
import ai.timefold.solver.benchmarks.micro.moveprovider.jmh.AbstractMoveProviderBenchmark;
import ai.timefold.solver.core.preview.api.domain.metamodel.PlanningSolutionMetaModel;
import ai.timefold.solver.core.preview.api.move.builtin.AssignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.ChangeMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.MassAssignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.MassChangeMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.MassUnassignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.PillarChangeMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.PillarSwapMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.PillarUnassignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.SubPillarChangeMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.SubPillarSwapMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.SubPillarUnassignMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.SwapMoveProvider;
import ai.timefold.solver.core.preview.api.move.builtin.UnassignMoveProvider;
import ai.timefold.solver.core.preview.api.neighborhood.MoveProvider;
import ai.timefold.solver.core.preview.api.neighborhood.stream.dataset.sample.Samplers;

/**
 * The 13 built-in basic-variable move providers, per
 * {@code /home/agent/.claude/plans/zany-drifting-ocean.md}. {@code PILLAR_CHANGE} and
 * {@code SUB_PILLAR_CHANGE} key on {@code room} rather than {@code startingTimeGrain}: 400 meetings
 * over 1280 grains means grain-keyed pillars are singletons, and {@link Samplers#between(int, int)}
 * drains then discards any slice below its minimum. {@code PILLAR_SWAP} and {@code SUB_PILLAR_SWAP}
 * run on {@link Example#MEETING_SCHEDULING_DENSE} instead, the only shape with non-singleton
 * composite (room, startingTimeGrain) pillars.
 */
public enum BasicMoveProviderCase implements MoveProviderCase {

    CHANGE(Example.MEETING_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(MeetingAssignment.class);
        var grain = entity.basicVariable("startingTimeGrain");
        return new ChangeMoveProvider<>(grain);
    }),
    MASS_CHANGE(Example.MEETING_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(MeetingAssignment.class);
        var grain = entity.basicVariable("startingTimeGrain");
        return new MassChangeMoveProvider<>(grain,
                Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE, AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE));
    }),
    SWAP(Example.MEETING_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(MeetingAssignment.class);
        return new SwapMoveProvider<>(entity);
    }),
    PILLAR_CHANGE(Example.MEETING_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(MeetingAssignment.class);
        var room = entity.basicVariable("room");
        return new PillarChangeMoveProvider<>(room);
    }),
    SUB_PILLAR_CHANGE(Example.MEETING_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(MeetingAssignment.class);
        var room = entity.basicVariable("room");
        return new SubPillarChangeMoveProvider<>(room, Samplers.pillar(
                Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE,
                        AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE)));
    }),
    PILLAR_SWAP(Example.MEETING_SCHEDULING_DENSE, metaModel -> {
        var entity = metaModel.genuineEntity(MeetingAssignment.class);
        return new PillarSwapMoveProvider<>(entity);
    }),
    SUB_PILLAR_SWAP(Example.MEETING_SCHEDULING_DENSE, metaModel -> {
        var entity = metaModel.genuineEntity(MeetingAssignment.class);
        return new SubPillarSwapMoveProvider<>(entity,
                Samplers.pillar(
                        Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE,
                                AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE)),
                Samplers.pillar(
                        Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE,
                                AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE)));
    }),
    ASSIGN(Example.PATIENT_ADMISSION_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(BedDesignation.class);
        var bed = entity.basicVariable("bed");
        return new AssignMoveProvider<>(bed);
    }),
    UNASSIGN(Example.PATIENT_ADMISSION_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(BedDesignation.class);
        var bed = entity.basicVariable("bed");
        return new UnassignMoveProvider<>(bed);
    }),
    MASS_ASSIGN(Example.PATIENT_ADMISSION_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(BedDesignation.class);
        var bed = entity.basicVariable("bed");
        return new MassAssignMoveProvider<>(bed,
                Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE, AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE));
    }),
    MASS_UNASSIGN(Example.PATIENT_ADMISSION_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(BedDesignation.class);
        var bed = entity.basicVariable("bed");
        return new MassUnassignMoveProvider<>(bed,
                Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE, AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE));
    }),
    PILLAR_UNASSIGN(Example.PATIENT_ADMISSION_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(BedDesignation.class);
        var bed = entity.basicVariable("bed");
        return new PillarUnassignMoveProvider<>(bed);
    }),
    SUB_PILLAR_UNASSIGN(Example.PATIENT_ADMISSION_SCHEDULING, metaModel -> {
        var entity = metaModel.genuineEntity(BedDesignation.class);
        var bed = entity.basicVariable("bed");
        return new SubPillarUnassignMoveProvider<>(bed, Samplers.pillar(
                Samplers.between(AbstractMoveProviderBenchmark.MIN_SAMPLE_SIZE,
                        AbstractMoveProviderBenchmark.MAX_SAMPLE_SIZE)));
    });

    private final Example example;
    private final Function<PlanningSolutionMetaModel<?>, MoveProvider<?>> providerFactory;

    BasicMoveProviderCase(Example example, Function<PlanningSolutionMetaModel<?>, MoveProvider<?>> providerFactory) {
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
