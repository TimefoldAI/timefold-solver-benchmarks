package ai.timefold.solver.benchmarks.examples.tsp.domain;

import ai.timefold.solver.benchmarks.examples.tsp.domain.location.Location;

public class Domicile extends Standstill {

    private Location location;

    public Domicile() {
    }

    public Domicile(long id) {
        super(id);
    }

    @Override
    public long getDistanceToNextStandstill() {
        var next = getNextStandstill();
        if (next == null) {
            return 0;
        }
        return getDistanceTo(next);
    }

    public Domicile(long id, Location location) {
        this(id);
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    /**
     * @param standstill never null
     * @return a positive number, the distance multiplied by 1000 to avoid floating point arithmetic rounding errors
     */
    @Override
    public long getDistanceTo(Standstill standstill) {
        return location.getDistanceTo(standstill.getLocation());
    }

}
