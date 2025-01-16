package ai.timefold.solver.benchmarks.competitive.cvrplib;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import ai.timefold.solver.benchmarks.competitive.AbstractCompetitiveBenchmark;
import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.VehicleRoutingSolution;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.location.AirLocation;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.persistence.VehicleRoutingImporter;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.termination.AdaptiveTerminationConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationCompositionStyle;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;

public class Ratios
        extends AbstractCompetitiveBenchmark<CVRPLIBDataset, CVRPLIBConfiguration, VehicleRoutingSolution, HardSoftLongScore> {

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        // Sort datasets by their distance, roughly approximating complexity, and order by it.
        var map = Arrays.stream(CVRPLIBDataset.values())
                .filter(c -> !c.isLarge()) // Ignore the really massive ones that take forever.
                .collect(Collectors.groupingBy(CVRPLIBDataset::getBestKnownDistance,
                        TreeMap::new,
                        Collectors.toList()));

        // Select every 50th dataset, roughly 10 datasets in total. Gives a decent cut of small, mid and big.
        var set = EnumSet.noneOf(CVRPLIBDataset.class);
        int i = 0;
        for (var entry : map.entrySet()) {
            if (i % 50 == 0) {
                set.add(entry.getValue().get(0));
            }
            i++;
        }

        var main = new Ratios();
        var importer = main.createImporter();

        var ratios = new double[] { 0.1, 0.01, 0.005, 0.001, 0.0005, 0.0001 };
        for (var configuration : CVRPLIBConfiguration.values()) {
            // CE is single-threaded, EE runs 4 move threads.
            var parallelRuns = configuration == CVRPLIBConfiguration.ENTERPRISE_EDITION ? 1 : 4;
            try (var executorService = Executors.newFixedThreadPool(parallelRuns)) {
                for (var dataset : set) {
                    for (var value : ratios) {
                        executorService.submit(() -> {
                            var threshold = dataset.getBestKnownDistance();
                            var solverConfig = configuration.getSolverConfig(dataset)
                                    .withTerminationConfig(new TerminationConfig());
                            var lsPhase = solverConfig.getPhaseConfigList().get(1);
                            var bestKnownScore = HardSoftLongScore.ofSoft(-threshold.longValue());
                            lsPhase.setTerminationConfig(new TerminationConfig()
                                    .withTerminationCompositionStyle(TerminationCompositionStyle.OR)
                                    .withBestScoreLimit(bestKnownScore.toString())
                                    .withAdaptiveTerminationConfig(new AdaptiveTerminationConfig()
                                            .withGracePeriodSeconds(30L)
                                            .withMinimumImprovementRatio(value)));
                            var solution = importer.readSolution(dataset.getPath().toFile());
                            var solver = SolverFactory.<VehicleRoutingSolution> create(solverConfig)
                                    .buildSolver();
                            var nanotime = System.nanoTime();
                            var result = solver.solve(solution);
                            nanotime = System.nanoTime() - nanotime;
                            System.out.println(dataset + " " + configuration + " " + value + " "
                                    + Duration.ofNanos(nanotime).toMillis() + " "
                                    + main.extractDistance(dataset, result.getScore()));
                        });
                    }
                }
            }
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
