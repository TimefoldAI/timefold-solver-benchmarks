package ai.timefold.solver.benchmarks.examples.vehiclerouting.persistence;

import java.io.File;
import java.util.function.Function;
import java.util.stream.Collectors;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractJsonSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.VehicleRoutingSolution;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.location.DistanceType;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.location.RoadLocation;

public class VehicleRoutingSolutionFileIO extends
        AbstractJsonSolutionFileIO<VehicleRoutingSolution> {

    public VehicleRoutingSolutionFileIO() {
        super(VehicleRoutingSolution.class);
    }

    @Override
    public VehicleRoutingSolution read(File inputSolutionFile) {
        VehicleRoutingSolution vehicleRoutingSolution =
                super.read(inputSolutionFile);

        if (vehicleRoutingSolution.getDistanceType() == DistanceType.ROAD_DISTANCE) {
            deduplicateRoadLocations(vehicleRoutingSolution);
        }

        return vehicleRoutingSolution;
    }

    private void deduplicateRoadLocations(
            VehicleRoutingSolution vehicleRoutingSolution) {
        var roadLocationList = vehicleRoutingSolution.getLocationList().stream()
                .filter(location -> location instanceof RoadLocation)
                .map(location -> (RoadLocation) location)
                .toList();
        var locationsById = roadLocationList.stream()
                .collect(Collectors.toMap(
                        RoadLocation::getId,
                        Function.identity()));
        /*
         * Replace the duplicate RoadLocation instances in the travelDistanceMap by references to instances from
         * the locationList.
         */
        for (RoadLocation roadLocation : roadLocationList) {
            var newTravelDistanceMap = deduplicateMap(roadLocation.getTravelDistanceMap(),
                    locationsById, RoadLocation::getId);
            roadLocation.setTravelDistanceMap(newTravelDistanceMap);
        }
    }

}
