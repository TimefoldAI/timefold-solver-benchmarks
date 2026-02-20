package ai.timefold.solver.benchmarks.examples.vehiclerouting.score;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.sum;

import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Customer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.timewindowed.TimeWindowedCustomer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.timewindowed.TimeWindowedDepot;
import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

public class VehicleRoutingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                vehicleCapacity(factory),
                distanceToPreviousStandstillPossiblyWithReturnToDepot(factory),
                arrivalAfterMaxEndTime(factory),
                depotArrivalAfterMaxEndTime(factory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    protected Constraint vehicleCapacity(ConstraintFactory factory) {
        return factory.forEach(Customer.class)
                .filter(customer -> customer.getVehicle() != null)
                .groupBy(Customer::getVehicle, sum(Customer::getDemand))
                .filter((vehicle, demand) -> demand > vehicle.getCapacity())
                .penalizeLong(HardSoftScore.ONE_HARD,
                        (vehicle, demand) -> demand - vehicle.getCapacity())
                .asConstraint("vehicleCapacity");
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    protected Constraint distanceToPreviousStandstillPossiblyWithReturnToDepot(ConstraintFactory factory) {
        return factory.forEach(Customer.class)
                .filter(customer -> customer.getVehicle() != null)
                .penalizeLong(HardSoftScore.ONE_SOFT, customer -> {
                    var distance = customer.getDistanceFromPreviousStandstill();
                    if (customer.getNextCustomer() == null) {
                        distance += customer.getDistanceToDepot();
                    }
                    return distance;
                }).asConstraint("distanceToPreviousStandstillPossiblyWithReturnToDepot");
    }

    // ************************************************************************
    // TimeWindowed: additional hard constraints
    // ************************************************************************

    protected Constraint arrivalAfterMaxEndTime(ConstraintFactory factory) {
        return factory.forEach(TimeWindowedCustomer.class)
                .filter(customer -> customer.getVehicle() != null)
                .filter(customer -> customer.getArrivalTime() > customer.getMaxEndTime())
                .penalizeLong(HardSoftScore.ONE_HARD,
                        customer -> customer.getArrivalTime() - customer.getMaxEndTime())
                .asConstraint("arrivalAfterMaxEndTime");
    }

    protected Constraint depotArrivalAfterMaxEndTime(ConstraintFactory factory) {
        return factory.forEach(TimeWindowedCustomer.class)
                .filter(customer -> customer.getVehicle() != null)
                .filter(customer -> customer.getNextCustomer() == null && getDepotArrivalDifference(customer) > 0)
                .penalizeLong(HardSoftScore.ONE_HARD,
                        VehicleRoutingConstraintProvider::getDepotArrivalDifference)
                .asConstraint("depotArrivalAfterMaxEndTime");
    }

    private static long getDepotArrivalDifference(TimeWindowedCustomer customer) {
        var depotMaxEndTime = ((TimeWindowedDepot) customer.getVehicle().getDepot()).getMaxEndTime();
        var arrivalTime = customer.getArrivalAtDepot();
        return arrivalTime - depotMaxEndTime;
    }

}
