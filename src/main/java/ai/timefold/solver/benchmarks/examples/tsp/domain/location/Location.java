package ai.timefold.solver.benchmarks.examples.tsp.domain.location;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;
import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AirLocation.class, name = "air"),
        @JsonSubTypes.Type(value = RoadLocation.class, name = "road"),
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public abstract class Location extends AbstractPersistable {

    protected double latitude;
    protected double longitude;

    protected Location() {
    }

    protected Location(long id) {
        super(id);
    }

    protected Location(long id, double latitude, double longitude) {
        this(id);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    /**
     * The distance's unit of measurement depends on the {@link TspSolution}'s {@link DistanceType}.
     * It can be in miles or km, but for most cases it's in the TSPLIB's unit of measurement.
     *
     * @param location never null
     * @return a positive number, the distance multiplied by 1000 to avoid floating point arithmetic rounding errors
     */
    @JsonIgnore
    public abstract long getDistanceTo(Location location);

    @JsonIgnore
    public double getAirDistanceDoubleTo(Location location) {
        // Implementation specified by TSPLIB http://www2.iwr.uni-heidelberg.de/groups/comopt/software/TSPLIB95/
        // Euclidean distance (Pythagorean theorem) - not correct when the surface is a sphere
        double latitudeDifference = location.latitude - latitude;
        double longitudeDifference = location.longitude - longitude;
        return Math.sqrt((latitudeDifference * latitudeDifference) + (longitudeDifference * longitudeDifference));
    }

    /**
     * The angle relative to the direction EAST.
     *
     * @param location never null
     * @return in Cartesian coordinates
     */
    @JsonIgnore
    public double getAngle(Location location) {
        // Euclidean distance (Pythagorean theorem) - not correct when the surface is a sphere
        double latitudeDifference = location.latitude - latitude;
        double longitudeDifference = location.longitude - longitude;
        return Math.atan2(latitudeDifference, longitudeDifference);
    }

    protected static long adjust(double distance) {
        return (long) (distance + 0.5); // +0.5 to avoid floating point rounding errors
    }

}
