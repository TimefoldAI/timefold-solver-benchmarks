package ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.location;

/**
 * CVRPTW datasets operate with precision, whereas CVRP are defined without it and should use {@link AirLocation}.
 */
public class TimeWindowedAirLocation extends Location {

    public static final double MULTIPLIER = 10L;

    public TimeWindowedAirLocation() {
    }

    public TimeWindowedAirLocation(long id, double latitude, double longitude) {
        super(id, latitude, longitude);
    }

    @Override
    public long getDistanceTo(Location location) {
        double distance = getAirDistanceDoubleTo(location);
        return (long) (MULTIPLIER * distance); // Truncate, do not round!
    }

}
