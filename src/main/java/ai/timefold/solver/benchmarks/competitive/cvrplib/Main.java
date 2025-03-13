package ai.timefold.solver.benchmarks.competitive.cvrplib;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ai.timefold.solver.benchmarks.competitive.AbstractCompetitiveBenchmark;
import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.VehicleRoutingSolution;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.location.AirLocation;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.persistence.VehicleRoutingImporter;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

public class Main
        extends AbstractCompetitiveBenchmark<CVRPLIBDataset, CVRPLIBConfiguration, VehicleRoutingSolution, HardSoftLongScore> {

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        var benchmark = new Main();
        if (args.length == 0) {
            benchmark.run(List.of(CVRPLIBConfiguration.COMMUNITY_EDITION), System.nanoTime(), CVRPLIBDataset.CVRPTWInstances());
        } else {
            var configuration = CVRPLIBConfiguration.valueOf(args[0]);
            int locationCount = Integer.parseInt(args[1]);
            long seed = Long.parseLong(args[2]);
            var datasets = Arrays.stream(CVRPLIBDataset.CVRPTWInstances()).filter(d -> {
                var initialSolution = benchmark.readInputFile(d.getPath().toFile());
                return benchmark.countLocations(initialSolution) == locationCount;
            }).toArray(CVRPLIBDataset[]::new);
            benchmark.run(List.of(configuration), seed, datasets);
        }
    }

    @Override
    protected String getLibraryName() {
        return "CVRPLIB";
    }

    @Override
    protected HardSoftLongScore extractScore(VehicleRoutingSolution vehicleRoutingSolution) {
        return vehicleRoutingSolution.getScore();
    }

    @Override
    protected BigDecimal extractDistance(CVRPLIBDataset dataset, HardSoftLongScore score) {
        return BigDecimal.valueOf(-score.softScore())
                .divide(BigDecimal.valueOf(AirLocation.MULTIPLIER), 1, RoundingMode.HALF_EVEN);
    }

    @Override
    protected int countLocations(VehicleRoutingSolution vehicleRoutingSolution) {
        return vehicleRoutingSolution.getCustomerList().size();
    }

    @Override
    protected int countVehicles(VehicleRoutingSolution vehicleRoutingSolution) {
        return vehicleRoutingSolution.getVehicleList().size();
    }

    @Override
    protected AbstractSolutionImporter<VehicleRoutingSolution> createImporter() {
        return new VehicleRoutingImporter();
    }
}
