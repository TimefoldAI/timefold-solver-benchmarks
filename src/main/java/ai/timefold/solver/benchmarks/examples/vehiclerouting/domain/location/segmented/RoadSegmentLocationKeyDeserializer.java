package ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.location.segmented;

import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.AbstractKeyDeserializer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.persistence.VehicleRoutingSolutionFileIO;

/**
 * @see VehicleRoutingSolutionFileIO
 */
final class RoadSegmentLocationKeyDeserializer extends AbstractKeyDeserializer<RoadSegmentLocation> {

    public RoadSegmentLocationKeyDeserializer() {
        super(RoadSegmentLocation.class);
    }

    @Override
    protected RoadSegmentLocation createInstance(long id) {
        return new RoadSegmentLocation(id);
    }
}
