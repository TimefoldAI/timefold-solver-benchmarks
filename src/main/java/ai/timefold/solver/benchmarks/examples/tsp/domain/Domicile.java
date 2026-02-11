package ai.timefold.solver.benchmarks.examples.tsp.domain;

import ai.timefold.solver.benchmarks.examples.tsp.domain.location.Location;
import ai.timefold.solver.benchmarks.examples.tsp.domain.location.LocationAware;

public class Domicile implements LocationAware {

    private long id;
    private Location location;

    public Domicile() {
    }

    public Domicile(long id, Location location) {
        this.id = id;
        this.location = location;
    }

    public Domicile(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

}
