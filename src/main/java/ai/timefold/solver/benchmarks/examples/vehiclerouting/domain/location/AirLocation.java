package ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.location;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The cost between 2 locations is a straight line: the euclidean distance between their GPS coordinates.
 * Used with {@link DistanceType#AIR_DISTANCE}.
 */
public class AirLocation extends Location {

    public static final double MULTIPLIER = 10L;

    // The default representation of the location just uses ints.
    // The double-precision representation is only for competitive benchmarks, and will never be serialized.
    @JsonIgnore
    private boolean truncate; // Some variants of CVRP and CVRPTW require different location handling.

    public AirLocation() {
    }

    public AirLocation(long id, double latitude, double longitude) {
        this(id, latitude, longitude, false);
    }

    public AirLocation(long id, double latitude, double longitude, boolean truncate) {
        super(id, latitude, longitude);
        this.truncate = truncate;
    }

    @Override
    public long getDistanceTo(Location location) {
        var distance = getAirDistanceDoubleTo(location);
        if (truncate) {
            return (long) (distance * MULTIPLIER);
        } else {
            return adjust(distance) * (int) MULTIPLIER;
        }
    }

}
