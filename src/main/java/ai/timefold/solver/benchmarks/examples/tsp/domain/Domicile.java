package ai.timefold.solver.benchmarks.examples.tsp.domain;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.tsp.domain.location.Location;
import ai.timefold.solver.benchmarks.examples.tsp.domain.location.LocationAware;

public class Domicile extends AbstractPersistable implements LocationAware {

    private Location location;

    public Domicile() {
    }

    public Domicile(long id) {
        this(id, null);
    }

    public Domicile(long id, Location location) {
        super(id);
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

}
