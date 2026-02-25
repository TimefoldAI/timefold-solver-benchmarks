package ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.solver;

import java.util.Comparator;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Customer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.VehicleRoutingSolution;
import ai.timefold.solver.core.api.domain.common.ComparatorFactory;

/**
 * On large datasets, the constructed solution looks like pizza slices.
 */
public class DepotAngleCustomerComparatorFactory implements ComparatorFactory<VehicleRoutingSolution, Customer> {

    @Override
    public Comparator<Customer> createComparator(VehicleRoutingSolution vehicleRoutingSolution) {
        var depot = vehicleRoutingSolution.getDepotList().get(0);
        return Comparator
                .<Customer> comparingDouble(customer -> customer.getLocation().getAngle(depot.getLocation()))
                // Ascending (further from the depot are more difficult)
                .thenComparingLong(customer -> customer.getLocation().getDistanceTo(depot.getLocation())
                        + depot.getLocation().getDistanceTo(customer.getLocation()))
                .thenComparingLong(AbstractPersistable::getId);
    }
}
