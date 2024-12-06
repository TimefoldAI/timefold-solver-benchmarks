package ai.timefold.solver.benchmarks.competitive.cvrplib;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import ai.timefold.solver.benchmarks.competitive.AbstractCompetitiveBenchmark;
import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.VehicleRoutingSolution;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.persistence.VehicleRoutingImporter;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

public class Main
        extends AbstractCompetitiveBenchmark<CVRPLIBDataset, CVRPLIBConfiguration, VehicleRoutingSolution, HardSoftLongScore> {

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        var benchmark = new Main();
        benchmark.run(CVRPLIBConfiguration.COMMUNITY_EDITION, CVRPLIBConfiguration.COMMUNITY_EDITION_TWEAKED,
                CVRPLIBConfiguration.ENTERPRISE_EDITION, CVRPLIBDataset.values());
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
    protected long extractDistance(HardSoftLongScore score) {
        return -score.softScore();
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
