package ai.timefold.solver.benchmarks.examples.vehiclerouting.app;

import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.examples.common.app.SolverSmokeTest;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.VehicleRoutingSolution;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.ConstraintStreamImplType;

class VehicleRoutingSmokeTest extends SolverSmokeTest<VehicleRoutingSolution, HardSoftLongScore> {

    private static final String CVRP_32_CUSTOMERS = "data/vehiclerouting/unsolved/cvrp-32customers.json";
    private static final String CVRPTW_100_CUSTOMERS_A = "data/vehiclerouting/unsolved/cvrptw-100customers-A.json";

    @Override
    protected VehicleRoutingApp createCommonApp() {
        return new VehicleRoutingApp();
    }

    @Override
    protected Stream<TestData<HardSoftLongScore>> testData() {
        return Stream.of(
                TestData.of(ConstraintStreamImplType.BAVET, CVRP_32_CUSTOMERS,
                        HardSoftLongScore.ofSoft(-744),
                        HardSoftLongScore.ofSoft(-744)),
                TestData.of(ConstraintStreamImplType.BAVET, CVRPTW_100_CUSTOMERS_A,
                        HardSoftLongScore.ofSoft(-1661),
                        HardSoftLongScore.ofSoft(-1661)));
    }
}
