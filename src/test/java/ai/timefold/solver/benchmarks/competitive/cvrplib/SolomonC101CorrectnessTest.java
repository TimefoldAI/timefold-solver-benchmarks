package ai.timefold.solver.benchmarks.competitive.cvrplib;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ai.timefold.solver.benchmarks.examples.common.domain.location.TimeWindowedAirLocation;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Customer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Vehicle;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.VehicleRoutingSolution;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.timewindowed.TimeWindowedCustomer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.timewindowed.TimeWindowedDepot;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.timewindowed.TimeWindowedVehicleRoutingSolution;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.persistence.VehicleRoutingImporter;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.solver.ScoreAnalysisFetchPolicy;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverFactory;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

class SolomonC101CorrectnessTest {

    @Test
    void runAndCheckScore() {
        // Load the dataset.
        var dataset = CVRPLIBDataset.C101;
        var solution =
                (TimeWindowedVehicleRoutingSolution) new VehicleRoutingImporter().readSolution(dataset.getPath().toFile());

        // Verify that the solution matches the dataset.
        var customers = solution.getCustomerList()
                .stream()
                .map(TimeWindowedCustomer.class::cast)
                .collect(Collectors.toList());
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(solution.getVehicleList()).hasSize(25);
            softly.assertThat(solution.getVehicleList())
                    .extracting(Vehicle::getCapacity)
                    .containsOnly(200);
            softly.assertThat(customers).hasSize(100);
        });

        SoftAssertions.assertSoftly(softly -> {
            var depot = (TimeWindowedDepot) solution.getDepotList().get(0);
            softly.assertThat(depot.getLocation().getLatitude()).isEqualTo(40);
            softly.assertThat(depot.getLocation().getLongitude()).isEqualTo(50);
            softly.assertThat(depot.getMinStartTime()).isEqualTo(0);
            softly.assertThat(depot.getMaxEndTime()).isEqualTo(Math.round(1236 * TimeWindowedAirLocation.MULTIPLIER));
        });

        verifyCustomer(customers, 1, 45, 68, 10, 912, 967, 90);
        verifyCustomer(customers, 2, 45, 70, 30, 825, 870, 90);
        verifyCustomer(customers, 3, 42, 66, 10, 65, 146, 90);
        verifyCustomer(customers, 4, 42, 68, 10, 727, 782, 90);
        verifyCustomer(customers, 5, 42, 65, 10, 15, 67, 90);
        verifyCustomer(customers, 6, 40, 69, 20, 621, 702, 90);
        verifyCustomer(customers, 7, 40, 66, 20, 170, 225, 90);
        verifyCustomer(customers, 8, 38, 68, 20, 255, 324, 90);
        verifyCustomer(customers, 9, 38, 70, 10, 534, 605, 90);
        verifyCustomer(customers, 10, 35, 66, 10, 357, 410, 90);
        verifyCustomer(customers, 11, 35, 69, 10, 448, 505, 90);
        verifyCustomer(customers, 12, 25, 85, 20, 652, 721, 90);
        verifyCustomer(customers, 13, 22, 75, 30, 30, 92, 90);
        verifyCustomer(customers, 14, 22, 85, 10, 567, 620, 90);
        verifyCustomer(customers, 15, 20, 80, 40, 384, 429, 90);
        verifyCustomer(customers, 16, 20, 85, 40, 475, 528, 90);
        verifyCustomer(customers, 17, 18, 75, 20, 99, 148, 90);
        verifyCustomer(customers, 18, 15, 75, 20, 179, 254, 90);
        verifyCustomer(customers, 19, 15, 80, 10, 278, 345, 90);
        verifyCustomer(customers, 20, 30, 50, 10, 10, 73, 90);
        verifyCustomer(customers, 21, 30, 52, 20, 914, 965, 90);
        verifyCustomer(customers, 22, 28, 52, 20, 812, 883, 90);
        verifyCustomer(customers, 23, 28, 55, 10, 732, 777, 90);
        verifyCustomer(customers, 24, 25, 50, 10, 65, 144, 90);
        verifyCustomer(customers, 25, 25, 52, 40, 169, 224, 90);
        verifyCustomer(customers, 26, 25, 55, 10, 622, 701, 90);
        verifyCustomer(customers, 27, 23, 52, 10, 261, 316, 90);
        verifyCustomer(customers, 28, 23, 55, 20, 546, 593, 90);
        verifyCustomer(customers, 29, 20, 50, 10, 358, 405, 90);
        verifyCustomer(customers, 30, 20, 55, 10, 449, 504, 90);
        verifyCustomer(customers, 31, 10, 35, 20, 200, 237, 90);
        verifyCustomer(customers, 32, 10, 40, 30, 31, 100, 90);
        verifyCustomer(customers, 33, 8, 40, 40, 87, 158, 90);
        verifyCustomer(customers, 34, 8, 45, 20, 751, 816, 90);
        verifyCustomer(customers, 35, 5, 35, 10, 283, 344, 90);
        verifyCustomer(customers, 36, 5, 45, 10, 665, 716, 90);
        verifyCustomer(customers, 37, 2, 40, 20, 383, 434, 90);
        verifyCustomer(customers, 38, 0, 40, 30, 479, 522, 90);
        verifyCustomer(customers, 39, 0, 45, 20, 567, 624, 90);
        verifyCustomer(customers, 40, 35, 30, 10, 264, 321, 90);
        verifyCustomer(customers, 41, 35, 32, 10, 166, 235, 90);
        verifyCustomer(customers, 42, 33, 32, 20, 68, 149, 90);
        verifyCustomer(customers, 43, 33, 35, 10, 16, 80, 90);
        verifyCustomer(customers, 44, 32, 30, 10, 359, 412, 90);
        verifyCustomer(customers, 45, 30, 30, 10, 541, 600, 90);
        verifyCustomer(customers, 46, 30, 32, 30, 448, 509, 90);
        verifyCustomer(customers, 47, 30, 35, 10, 1054, 1127, 90);
        verifyCustomer(customers, 48, 28, 30, 10, 632, 693, 90);
        verifyCustomer(customers, 49, 28, 35, 10, 1001, 1066, 90);
        verifyCustomer(customers, 50, 26, 32, 10, 815, 880, 90);
        verifyCustomer(customers, 51, 25, 30, 10, 725, 786, 90);
        verifyCustomer(customers, 52, 25, 35, 10, 912, 969, 90);
        verifyCustomer(customers, 53, 44, 5, 20, 286, 347, 90);
        verifyCustomer(customers, 54, 42, 10, 40, 186, 257, 90);
        verifyCustomer(customers, 55, 42, 15, 10, 95, 158, 90);
        verifyCustomer(customers, 56, 40, 5, 30, 385, 436, 90);
        verifyCustomer(customers, 57, 40, 15, 40, 35, 87, 90);
        verifyCustomer(customers, 58, 38, 5, 30, 471, 534, 90);
        verifyCustomer(customers, 59, 38, 15, 10, 651, 740, 90);
        verifyCustomer(customers, 60, 35, 5, 20, 562, 629, 90);
        verifyCustomer(customers, 61, 50, 30, 10, 531, 610, 90);
        verifyCustomer(customers, 62, 50, 35, 20, 262, 317, 90);
        verifyCustomer(customers, 63, 50, 40, 50, 171, 218, 90);
        verifyCustomer(customers, 64, 48, 30, 10, 632, 693, 90);
        verifyCustomer(customers, 65, 48, 40, 10, 76, 129, 90);
        verifyCustomer(customers, 66, 47, 35, 10, 826, 875, 90);
        verifyCustomer(customers, 67, 47, 40, 10, 12, 77, 90);
        verifyCustomer(customers, 68, 45, 30, 10, 734, 777, 90);
        verifyCustomer(customers, 69, 45, 35, 10, 916, 969, 90);
        verifyCustomer(customers, 70, 95, 30, 30, 387, 456, 90);
        verifyCustomer(customers, 71, 95, 35, 20, 293, 360, 90);
        verifyCustomer(customers, 72, 53, 30, 10, 450, 505, 90);
        verifyCustomer(customers, 73, 92, 30, 10, 478, 551, 90);
        verifyCustomer(customers, 74, 53, 35, 50, 353, 412, 90);
        verifyCustomer(customers, 75, 45, 65, 20, 997, 1068, 90);
        verifyCustomer(customers, 76, 90, 35, 10, 203, 260, 90);
        verifyCustomer(customers, 77, 88, 30, 10, 574, 643, 90);
        verifyCustomer(customers, 78, 88, 35, 20, 109, 170, 90);
        verifyCustomer(customers, 79, 87, 30, 10, 668, 731, 90);
        verifyCustomer(customers, 80, 85, 25, 10, 769, 820, 90);
        verifyCustomer(customers, 81, 85, 35, 30, 47, 124, 90);
        verifyCustomer(customers, 82, 75, 55, 20, 369, 420, 90);
        verifyCustomer(customers, 83, 72, 55, 10, 265, 338, 90);
        verifyCustomer(customers, 84, 70, 58, 20, 458, 523, 90);
        verifyCustomer(customers, 85, 68, 60, 30, 555, 612, 90);
        verifyCustomer(customers, 86, 66, 55, 10, 173, 238, 90);
        verifyCustomer(customers, 87, 65, 55, 20, 85, 144, 90);
        verifyCustomer(customers, 88, 65, 60, 30, 645, 708, 90);
        verifyCustomer(customers, 89, 63, 58, 10, 737, 802, 90);
        verifyCustomer(customers, 90, 60, 55, 10, 20, 84, 90);
        verifyCustomer(customers, 91, 60, 60, 10, 836, 889, 90);
        verifyCustomer(customers, 92, 67, 85, 20, 368, 441, 90);
        verifyCustomer(customers, 93, 65, 85, 40, 475, 518, 90);
        verifyCustomer(customers, 94, 65, 82, 10, 285, 336, 90);
        verifyCustomer(customers, 95, 62, 80, 30, 196, 239, 90);
        verifyCustomer(customers, 96, 60, 80, 10, 95, 156, 90);
        verifyCustomer(customers, 97, 60, 85, 30, 561, 622, 90);
        verifyCustomer(customers, 98, 58, 75, 20, 30, 84, 90);
        verifyCustomer(customers, 99, 55, 80, 10, 743, 820, 90);
        verifyCustomer(customers, 100, 55, 85, 20, 647, 726, 90);

        // Set the dataset to the best known solution.
        var vehicles = solution.getVehicleList();
        vehicles.get(0)
                .setCustomers(assembleCustomers(solution, 5, 3, 7, 8, 10, 11, 9, 6, 4, 2, 1, 75));
        vehicles.get(1)
                .setCustomers(assembleCustomers(solution, 13, 17, 18, 19, 15, 16, 14, 12));
        vehicles.get(2)
                .setCustomers(assembleCustomers(solution, 20, 24, 25, 27, 29, 30, 28, 26, 23, 22, 21));
        vehicles.get(3)
                .setCustomers(assembleCustomers(solution, 32, 33, 31, 35, 37, 38, 39, 36, 34));
        vehicles.get(4)
                .setCustomers(assembleCustomers(solution, 43, 42, 41, 40, 44, 46, 45, 48, 51, 50, 52, 49, 47));
        vehicles.get(5)
                .setCustomers(assembleCustomers(solution, 57, 55, 54, 53, 56, 58, 60, 59));
        vehicles.get(6)
                .setCustomers(assembleCustomers(solution, 67, 65, 63, 62, 74, 72, 61, 64, 68, 66, 69));
        vehicles.get(7)
                .setCustomers(assembleCustomers(solution, 81, 78, 76, 71, 70, 73, 77, 79, 80));
        vehicles.get(8)
                .setCustomers(assembleCustomers(solution, 90, 87, 86, 83, 82, 84, 85, 88, 89, 91));
        vehicles.get(9)
                .setCustomers(assembleCustomers(solution, 98, 96, 95, 94, 92, 93, 97, 100, 99));

        // Check the score of the solution.
        var config = CVRPLIBConfiguration.ENTERPRISE_EDITION.getSolverConfig(dataset);
        var solutionManager = SolutionManager.<VehicleRoutingSolution, HardSoftLongScore> create(SolverFactory.create(config));
        var analysis = solutionManager.analyze(solution, ScoreAnalysisFetchPolicy.FETCH_ALL);
        var score = analysis.score();
        System.out.println("Score: " + score);

        assertThat(score.isFeasible()).isTrue();
        assertThat(convertScore(-score.softScore()))
                .isEqualTo(827.3, Offset.offset(0.1));
    }

    private void verifyCustomer(List<TimeWindowedCustomer> customers, int customerId, int x, int y, int demand, int readyTime,
                                int dueDate, int serviceTime) {
        var customer = customers.get(customerId - 1);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(customer.getLocation().getLatitude()).isEqualTo(x);
            softly.assertThat(customer.getLocation().getLongitude()).isEqualTo(y);
            softly.assertThat(customer.getDemand()).isEqualTo(demand);
            softly.assertThat(customer.getMinStartTime()).isEqualTo(Math.round(readyTime * TimeWindowedAirLocation.MULTIPLIER));
            softly.assertThat(customer.getMaxEndTime()).isEqualTo(Math.round(dueDate * TimeWindowedAirLocation.MULTIPLIER));
            softly.assertThat(customer.getServiceDuration()).isEqualTo(Math.round(serviceTime * TimeWindowedAirLocation.MULTIPLIER));
        });
    }

    private double convertScore(long score) {
        return BigDecimal.valueOf(score)
                .divide(BigDecimal.valueOf(TimeWindowedAirLocation.MULTIPLIER), 1, RoundingMode.HALF_EVEN)
                .doubleValue();
    }

    private List<Customer> assembleCustomers(VehicleRoutingSolution solution, int... customerIds) {
        return IntStream.of(customerIds)
                .mapToObj(i -> solution.getCustomerList().get(i - 1))
                .collect(Collectors.toList());
    }

}
