package ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.solver;

import java.util.Comparator;

import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Customer;

/**
 * On large datasets, the constructed solution looks like a zebra crossing.
 */
public class LatitudeCustomerDifficultyComparator
        implements Comparator<Customer> {

    private static final Comparator<Customer> COMPARATOR =
            Comparator
                    .comparingDouble(
                            (Customer customer) -> customer
                                    .getLocation().getLatitude())
                    .thenComparingDouble(customer -> customer.getLocation().getLongitude())
                    .thenComparingInt(Customer::getDemand)
                    .thenComparingLong(Customer::getId);

    @Override
    public int compare(Customer a, Customer b) {
        return COMPARATOR.compare(a, b);
    }

}
