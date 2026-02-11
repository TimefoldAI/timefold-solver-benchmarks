package ai.timefold.solver.benchmarks.examples.tsp.domain;

import ai.timefold.solver.benchmarks.examples.tsp.domain.location.Location;
import ai.timefold.solver.benchmarks.examples.tsp.domain.location.LocationAware;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.NextElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
public class Visit implements LocationAware {

    @PlanningId
    private long id;

    private Location location;

    @InverseRelationShadowVariable(sourceVariableName = "visits")
    private Tour tour;
    @PreviousElementShadowVariable(sourceVariableName = "visits")
    private Visit previous;
    @NextElementShadowVariable(sourceVariableName = "visits")
    private Visit next;

    public Visit() {
    }

    public Visit(long id, Location location) {
        this.id = id;
        this.location = location;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public Visit getNext() {
        return next;
    }

    public void setNext(Visit next) {
        this.next = next;
    }

    public Visit getPrevious() {
        return previous;
    }

    public void setPrevious(Visit previous) {
        this.previous = previous;
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

    @JsonIgnore
    public long getDistanceFromPreviousVisit() {
        if (tour == null) {
            throw new IllegalStateException(
                    "This method must not be called when the shadow variables are not initialized yet.");
        }
        if (previous == null) {
            return getDistanceToDepot();
        }
        return previous.getLocation().getDistanceTo(location);
    }

    @JsonIgnore
    public long getDistanceToDepot() {
        return location.getDistanceTo(tour.getDomicile().getLocation());
    }

}
