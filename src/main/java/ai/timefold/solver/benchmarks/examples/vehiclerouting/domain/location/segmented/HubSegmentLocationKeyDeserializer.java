package ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.location.segmented;

import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.AbstractKeyDeserializer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.persistence.VehicleRoutingSolutionFileIO;

/**
 * @see VehicleRoutingSolutionFileIO
 */
final class HubSegmentLocationKeyDeserializer extends AbstractKeyDeserializer<HubSegmentLocation> {

    public HubSegmentLocationKeyDeserializer() {
        super(HubSegmentLocation.class);
    }

    @Override
    protected HubSegmentLocation createInstance(long id) {
        return new HubSegmentLocation(id);
    }
}
