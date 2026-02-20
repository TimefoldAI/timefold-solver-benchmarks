package ai.timefold.solver.benchmarks.competitive.cvrplib;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Customer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.VehicleRoutingSolution;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.persistence.VehicleRoutingImporter;
import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.solver.ScoreAnalysisFetchPolicy;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverFactory;

import org.junit.jupiter.api.Test;

class AN33K6CorrectnessTest {

    @Test
    void runAndCheckScore() {
        // Load the dataset.
        var dataset = CVRPLIBDataset.A_N33_K6;
        var solution = new VehicleRoutingImporter().readSolution(dataset.getPath().toFile());

        // Set the dataset to the best known solution.
        var vehicles = solution.getVehicleList();
        vehicles.get(0)
                .setCustomers(assembleCustomers(solution, 5, 2, 20, 15, 9, 3, 8, 4));
        vehicles.get(1)
                .setCustomers(assembleCustomers(solution, 31, 24, 23, 26, 22));
        vehicles.get(2)
                .setCustomers(assembleCustomers(solution, 17, 11, 29, 19, 7));
        vehicles.get(3)
                .setCustomers(assembleCustomers(solution, 10, 12, 21));
        vehicles.get(4)
                .setCustomers(assembleCustomers(solution, 28, 27, 30, 16, 25, 32));
        vehicles.get(5)
                .setCustomers(assembleCustomers(solution, 13, 6, 18, 1, 14));

        // Check the score of the solution.
        var config = CVRPLIBConfiguration.ENTERPRISE_EDITION.getSolverConfig(dataset);
        var solutionManager = SolutionManager.<VehicleRoutingSolution, HardSoftScore> create(SolverFactory.create(config));
        var analysis = solutionManager.analyze(solution, ScoreAnalysisFetchPolicy.FETCH_ALL);
        var score = analysis.score();
        assertThat(score).isEqualTo(HardSoftScore.of(0, -7420));
    }

    private List<Customer> assembleCustomers(VehicleRoutingSolution solution, int... customerIds) {
        return IntStream.of(customerIds)
                .mapToObj(i -> solution.getCustomerList().get(i - 1))
                .collect(Collectors.toList());
    }

}
