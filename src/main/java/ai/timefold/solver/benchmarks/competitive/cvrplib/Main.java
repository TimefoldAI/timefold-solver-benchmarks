package ai.timefold.solver.benchmarks.competitive.cvrplib;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ai.timefold.solver.benchmarks.competitive.AbstractCompetitiveBenchmark;
import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.VehicleRoutingSolution;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.location.AirLocation;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.persistence.VehicleRoutingImporter;
import ai.timefold.solver.core.api.score.HardSoftScore;

public class Main
        extends AbstractCompetitiveBenchmark<CVRPLIBDataset, CVRPLIBConfiguration, VehicleRoutingSolution, HardSoftScore> {

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        var benchmark = new Main();
        benchmark.run(List.of(CVRPLIBConfiguration.COMMUNITY_EDITION, CVRPLIBConfiguration.ENTERPRISE_EDITION),
                CVRPLIBDataset.values());
    }

    @Override
    protected String getLibraryName() {
        return "CVRPLIB";
    }

    @Override
    protected HardSoftScore extractScore(VehicleRoutingSolution vehicleRoutingSolution) {
        return vehicleRoutingSolution.getScore();
    }

    @Override
    protected BigDecimal extractResult(CVRPLIBDataset dataset, HardSoftScore score) {
        return BigDecimal.valueOf(-score.softScore())
                .divide(BigDecimal.valueOf(AirLocation.MULTIPLIER), 1, RoundingMode.HALF_EVEN);
    }

    @Override
    protected int countValues(VehicleRoutingSolution vehicleRoutingSolution) {
        return vehicleRoutingSolution.getCustomerList().size();
    }

    @Override
    protected int countEntities(VehicleRoutingSolution vehicleRoutingSolution) {
        return vehicleRoutingSolution.getVehicleList().size();
    }

    @Override
    protected AbstractSolutionImporter<VehicleRoutingSolution> createImporter() {
        return new VehicleRoutingImporter();
    }

    @Override
    public void enrichSolution(VehicleRoutingSolution vehicleRoutingSolution) {
        // No changes are applied to the solution
    }
}
