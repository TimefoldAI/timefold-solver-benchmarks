package ai.timefold.solver.benchmarks.examples.tsp.domain;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;
import ai.timefold.solver.benchmarks.examples.tsp.domain.location.Location;
import ai.timefold.solver.benchmarks.examples.tsp.domain.location.LocationAware;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.NextElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@PlanningEntity
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class Visit extends AbstractPersistable implements LocationAware {

    private Location location;

    @InverseRelationShadowVariable(sourceVariableName = "visitList")
    private Tour tour;
    @JsonIdentityReference(alwaysAsId = true)
    @PreviousElementShadowVariable(sourceVariableName = "visitList")
    private Visit previous;
    @JsonIdentityReference(alwaysAsId = true)
    @NextElementShadowVariable(sourceVariableName = "visitList")
    private Visit next;

    public Visit() {
    }

    public Visit(Long id, Location location) {
        super(id);
        this.location = location;
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
