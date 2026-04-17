package ai.timefold.solver.benchmarks.examples.vehiclerouting.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.location.Location;
import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.solver.phase.SplitPhase;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;

/**
 * Split algorithm based on the work:
 * Order-first split-second methods for vehicle routing problems: a review by C. Prins et al.
 */
public class SplitLoadPhase implements SplitPhase<VehicleRoutingSolution, HardSoftScore> {

    private int[] customGiantTour = new int[] { 37, 55, 2, 31, 71, 87, 32, 53, 26, 29, 73, 93, 20, 42, 92, 34, 13, 96, 46, 66,
            98, 19, 16, 25, 22, 40, 70, 84, 48, 33, 64, 89, 6, 10, 62, 39, 50, 77, 74, 100, 45, 18, 24, 95, 15, 28, 81, 86, 67,
            41, 83, 91, 76, 59, 38, 17, 3, 54, 63, 58, 78, 43, 57, 97, 36, 82, 5, 44, 94, 30, 14, 51, 23, 99, 88, 21, 35, 68,
            12, 80, 60, 7, 47, 11, 49, 90, 52, 85, 9, 72, 61, 75, 1, 65, 8, 79, 27, 4, 56, 69 };

    private Customer[] getCustomGiantTour(InnerScoreDirector<VehicleRoutingSolution, HardSoftScore> scoreDirector,
            VehicleRoutingSolution solution) {
        var giantTour = new Customer[customGiantTour.length];
        for (int i = 0; i < customGiantTour.length; i++) {
            giantTour[i] = solution.getCustomerList().get(customGiantTour[i] - 1);
        }
        return giantTour;
    }

    @Override
    public boolean splitLoad(InnerScoreDirector<VehicleRoutingSolution, HardSoftScore> scoreDirector,
            VehicleRoutingSolution solution, Object[] giantTour) {
        if (giantTour.length == 0) {
            return false;
        }
        var customerCount = giantTour.length;
        var customers = Arrays.copyOf(giantTour, customerCount, Customer[].class);
        //        var customers = getCustomGiantTour(scoreDirector, solution);

        var vehicles = solution.getVehicleList();
        var vehicleCount = vehicles.size();
        var capacity = vehicles.get(0).getCapacity();
        var capacityMax = 1.5 * vehicles.get(0).getCapacity();
        var depotLocation = solution.getDepotList().getFirst().getLocation();
        var splitContext = loadContext(depotLocation, vehicleCount, customers);

        // potential[k][i] = minimum cost to split customers[0..i-1] into exactly k routes
        var potential = new double[vehicleCount + 1][customerCount + 1];
        // predecessor[k][i] = start index of route k (customers[predecessor[k][i]..i-1])
        var predecessor = new int[vehicleCount + 1][customerCount + 1];
        for (var row : potential) {
            Arrays.fill(row, 1.e30);
        }
        potential[0][0] = 0;

        for (var k = 0; k < vehicleCount; k++) {
            for (var i = k; i < customerCount && potential[k][i] < 1.e29; i++) {
                var load = 0;
                var distance = 0.0;
                for (var j = i + 1; j <= customerCount && load <= capacityMax; j++) {
                    load += splitContext[j].demand();
                    if (j == i + 1) {
                        // Single-stop route: depot -> nextCustomer -> depot
                        distance += splitContext[j].distanceFromDepot();
                    } else {
                        // Extend route: replace "cPrev -> depot" with "cPrev -> nextCustomer -> depot"
                        distance += splitContext[j - 1].distanceToNext();
                    }
                    var cost = distance + splitContext[j].distanceToDepot() + Math.max(load - capacity, 0) * 12.65;
                    if (potential[k][i] + cost < potential[k + 1][j]) {
                        potential[k + 1][j] = potential[k][i] + cost;
                        predecessor[k + 1][j] = i;
                    }
                }
            }
        }

        if (potential[vehicleCount][customerCount] > 1.e29) {
            // No possible solution is possible with available vehicles and their capacity
            return false;
        }

        // It could be cheaper to use a smaller number of vehicles
        var minCost = potential[vehicleCount][customerCount];
        int bestK = vehicleCount;
        for (int k = 1; k < vehicleCount; k++) {
            if (potential[k][customerCount] < minCost) {
                minCost = potential[k][customerCount];
                bestK = k;
            }
        }

        // Backtrack to collect routes (in reverse order)
        var allRoutes = new List[bestK];
        int end = customerCount;
        for (int k = bestK - 1; k >= 0; k--) {
            int begin = predecessor[k + 1][end];
            var route = new ArrayList<>(end - begin);
            for (var idx = begin; idx < end; idx++) {
                route.add(customers[idx]);
            }
            allRoutes[k] = route;
            end = begin;
        }

        var listVariableDescriptor = scoreDirector.getSolutionDescriptor().getListVariableDescriptor();
        for (var i = 0; allRoutes.length > i; i++) {
            var vehicle = vehicles.get(i);
            scoreDirector.beforeListVariableChanged(vehicle, "customers", 0, 0);
            for (var value : allRoutes[i]) {
                scoreDirector.beforeListVariableElementAssigned(vehicle, "customers", value);
            }
            vehicle.getCustomers().addAll(allRoutes[i]);
            scoreDirector.afterListVariableChanged(vehicle, "customers", 0, vehicle.getCustomers().size());
            for (var value : allRoutes[i]) {
                scoreDirector.afterListVariableElementAssigned(vehicle, "customers", value);
            }
            scoreDirector.triggerVariableListeners();
        }
        return true;
    }

    private SplitContext[] loadContext(Location depot, int countVehicles, Customer[] giantTour) {
        var customerCount = giantTour.length;
        var context = new SplitContext[customerCount + 1];
        context[0] = new SplitContext(0, 0, 0, 0);
        for (var i = 1; i <= customerCount; i++) {
            var customer = giantTour[i - 1];
            var distanceToNext =
                    i < customerCount ? customer.getLocation().getDistanceTo(giantTour[i].getLocation()) / 10 : (long) -1.e30;
            context[i] = new SplitContext(customer.getDemand(), depot.getDistanceTo(customer.getLocation()) / 10,
                    customer.getLocation().getDistanceTo(depot) / 10, distanceToNext);
        }
        return context;
    }

    private record SplitContext(int demand, long distanceFromDepot, long distanceToDepot, long distanceToNext) {

    }

}
