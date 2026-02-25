package ai.timefold.solver.benchmarks.examples.vehiclerouting.app;

import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.examples.common.app.SolverSmokeTest;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.VehicleRoutingSolution;
import ai.timefold.solver.core.api.score.HardSoftScore;

class VehicleRoutingSmokeTest extends SolverSmokeTest<VehicleRoutingSolution, HardSoftScore> {

    private static final String CVRP_32_CUSTOMERS = "data/vehiclerouting/unsolved/cvrp-32customers.json";
    private static final String CVRPTW_100_CUSTOMERS_A = "data/vehiclerouting/unsolved/cvrptw-100customers-A.json";

    @Override
    protected VehicleRoutingApp createCommonApp() {
        return new VehicleRoutingApp();
    }

    @Override
    protected Stream<TestData<HardSoftScore>> testData() {
        return Stream.of(
                TestData.of(CVRP_32_CUSTOMERS,
                        HardSoftScore.ofSoft(-7440),
                        HardSoftScore.ofSoft(-7440)),
                TestData.of(CVRPTW_100_CUSTOMERS_A,
                        HardSoftScore.ofSoft(-16610),
                        HardSoftScore.ofSoft(-16610)));
    }
}
