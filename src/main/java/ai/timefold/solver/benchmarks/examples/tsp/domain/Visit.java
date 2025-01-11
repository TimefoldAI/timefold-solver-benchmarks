package ai.timefold.solver.benchmarks.examples.tsp.domain;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.tsp.domain.location.Location;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariableGraphType;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
public class Visit extends AbstractPersistable implements Standstill {

    private Location location;

    // Planning variables: changes during planning, between score calculations.
    private Standstill previousStandstill;

    public Visit() {
    }

    public Visit(long id, Location location) {
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

    @PlanningVariable(graphType = PlanningVariableGraphType.CHAINED)
    public Standstill getPreviousStandstill() {
        return previousStandstill;
    }

    public void setPreviousStandstill(Standstill previousStandstill) {
        this.previousStandstill = previousStandstill;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    /**
     * @return a positive number, the distance multiplied by 1000 to avoid floating point arithmetic rounding errors
     */
    @JsonIgnore
    public long getDistanceFromPreviousStandstill() {
        if (previousStandstill == null) {
            return 0L;
        }
        return getDistanceFrom(previousStandstill);
    }

    /**
     * @param standstill never null
     * @return a positive number, the distance multiplied by 1000 to avoid floating point arithmetic rounding errors
     */
    @JsonIgnore
    public long getDistanceFrom(Standstill standstill) {
        return standstill.getLocation().getDistanceTo(location);
    }

    /**
     * @param standstill never null
     * @return a positive number, the distance multiplied by 1000 to avoid floating point arithmetic rounding errors
     */
    @Override
    public long getDistanceTo(Standstill standstill) {
        return location.getDistanceTo(standstill.getLocation());
    }

}
