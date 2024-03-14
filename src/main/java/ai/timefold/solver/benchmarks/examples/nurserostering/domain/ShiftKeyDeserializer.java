package ai.timefold.solver.benchmarks.examples.nurserostering.domain;

import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.AbstractKeyDeserializer;

final class ShiftKeyDeserializer extends AbstractKeyDeserializer<Shift> {

    public ShiftKeyDeserializer() {
        super(Shift.class);
    }

    @Override
    protected Shift createInstance(long id) {
        return new Shift(id);
    }
}
