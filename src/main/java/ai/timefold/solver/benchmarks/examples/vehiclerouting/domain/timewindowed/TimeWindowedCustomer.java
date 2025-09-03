package ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.timewindowed;

import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Customer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.location.Location;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.ShadowSources;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
public class TimeWindowedCustomer extends Customer {

    // Times are multiplied by 1000 to avoid floating point arithmetic rounding errors
    private long minStartTime;
    private long maxEndTime;
    private long serviceDuration;

    // Shadow variable
    private Long arrivalTime;

    public TimeWindowedCustomer() {
    }

    public TimeWindowedCustomer(long id, Location location, int demand, long minStartTime, long maxEndTime,
            long serviceDuration) {
        super(id, location, demand);
        this.minStartTime = minStartTime;
        this.maxEndTime = maxEndTime;
        this.serviceDuration = serviceDuration;
    }

    /**
     * @return a positive number, the time multiplied by 1000 to avoid floating point arithmetic rounding errors
     */
    public long getMinStartTime() {
        return minStartTime;
    }

    public void setMinStartTime(long minStartTime) {
        this.minStartTime = minStartTime;
    }

    /**
     * @return a positive number, the time multiplied by 1000 to avoid floating point arithmetic rounding errors
     */
    public long getMaxEndTime() {
        return maxEndTime;
    }

    public void setMaxEndTime(long maxEndTime) {
        this.maxEndTime = maxEndTime;
    }

    /**
     * @return a positive number, the time multiplied by 1000 to avoid floating point arithmetic rounding errors
     */
    public long getServiceDuration() {
        return serviceDuration;
    }

    public void setServiceDuration(long serviceDuration) {
        this.serviceDuration = serviceDuration;
    }

    /**
     * @return a positive number, the time multiplied by 1000 to avoid floating point arithmetic rounding errors
     */
    // Arguable, to adhere to API specs (although this works), nextCustomer should also be a source,
    // because this shadow must be triggered after nextCustomer (but there is no need to be triggered by nextCustomer)
    @ShadowVariable(supplierName = "arrivalTimeSupplier")
    public Long getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Long arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    @ShadowSources({ "vehicle", "previousCustomer.arrivalTime" })
    public Long arrivalTimeSupplier() {
        if (vehicle == null) {
            return null;
        }

        Long departureTime;
        if (previousCustomer == null) {
            departureTime = ((TimeWindowedDepot) vehicle.getDepot()).getMinStartTime();
        } else {
            departureTime = getPreviousCustomer().getDepartureTime();
        }

        if (departureTime == null) {
            return null;
        }

        return departureTime + getDistanceFromPreviousStandstill();
    }

    @Override
    public TimeWindowedCustomer getPreviousCustomer() {
        return (TimeWindowedCustomer) super.getPreviousCustomer();
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    /**
     * @return a positive number, the time multiplied by 1000 to avoid floating point arithmetic rounding errors
     */
    public Long getDepartureTime() {
        if (arrivalTime == null) {
            return null;
        }
        return Math.max(arrivalTime, minStartTime) + serviceDuration;
    }

    @JsonIgnore
    public long getArrivalAtDepot() {
        return getDepartureTime() + getDistanceToDepot();
    }

}
