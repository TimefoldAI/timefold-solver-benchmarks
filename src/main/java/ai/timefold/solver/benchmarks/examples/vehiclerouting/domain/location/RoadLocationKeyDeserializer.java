package ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.location;

import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.AbstractKeyDeserializer;

final class RoadLocationKeyDeserializer
        extends AbstractKeyDeserializer<RoadLocation> {

    public RoadLocationKeyDeserializer() {
        super(RoadLocation.class);
    }

    @Override
    protected RoadLocation createInstance(long id) {
        return new RoadLocation(id);
    }
}
