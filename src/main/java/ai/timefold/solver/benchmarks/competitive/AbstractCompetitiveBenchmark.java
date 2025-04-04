package ai.timefold.solver.benchmarks.competitive;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.impl.score.director.InnerScore;
import ai.timefold.solver.core.impl.solver.DefaultSolverFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCompetitiveBenchmark<Dataset_ extends Dataset<Dataset_>, Configuration_ extends Configuration<Dataset_>, Solution_, Score_ extends Score<Score_>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCompetitiveBenchmark.class);

    public static final long MAX_SECONDS = 60;
    public static final long UNIMPROVED_SECONDS_TERMINATION = MAX_SECONDS / 3;

    static final int MAX_THREADS = 4; // Set to the number of performance cores on your machine.
    // Recommended to divide MAX_THREADS without remainder.
    // Don't overdo it with move threads; it's not a silver bullet.
    public static final int ENTERPRISE_MOVE_THREAD_COUNT = 4;

    protected abstract String getLibraryName();

    protected abstract Score_ extractScore(Solution_ solution);

    protected abstract BigDecimal extractDistance(Dataset_ dataset, Score_ score);

    protected abstract int countLocations(Solution_ solution);

    protected abstract int countVehicles(Solution_ solution);

    protected abstract AbstractSolutionImporter<Solution_> createImporter();

    public void run(Configuration_ communityEdition, Configuration_ enterpriseEdition,
            Dataset_... datasets)
            throws ExecutionException, InterruptedException, IOException {
        var communityResultList = run(communityEdition, datasets);
        var enterpriseResultList = run(enterpriseEdition, datasets);

        var result = new StringBuilder();
        try {
            String line = """
                    %s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s
                    """;
            String header = line.formatted("Dataset", "Location count", "Vehicle count", "Best known score",
                    "CE Achieved score", "CE run time (ms)", "CE gap to best (%)", "CE Health",
                    "EE Achieved score", "EE run time (ms)", "EE gap to best (%)", "EE Health");
            result.append(header);

            for (var dataset : datasets) {
                var communityResult = communityResultList.get(dataset);
                var enterpriseResult = enterpriseResultList.get(dataset);

                var datasetName = dataset.name();
                var communityInnerScore = communityResult.score();
                var communityRuntime = communityResult.runtime().toMillis();
                var communityGap = computeGap(dataset, communityInnerScore.initialized());
                var communityHealth = determineHealth(dataset, communityInnerScore, communityResult.runtime());
                var enterpriseInnerScore = enterpriseResult.score();
                var enterpriseRuntime = enterpriseResult.runtime().toMillis();
                var enterpriseTweakedGap = computeGap(dataset, enterpriseInnerScore.initialized());
                var enterpriseHealth = determineHealth(dataset, enterpriseInnerScore, enterpriseResult.runtime());
                result.append(line.formatted(
                        quote(datasetName),
                        communityResult.locationCount(),
                        communityResult.vehicleCount(),
                        roundToOneDecimal(dataset.getBestKnownDistance()),
                        roundToOneDecimal(extractDistance(dataset, communityInnerScore.initialized())),
                        communityRuntime,
                        communityGap,
                        quote(communityHealth),
                        roundToOneDecimal(extractDistance(dataset, enterpriseInnerScore.initialized())),
                        enterpriseRuntime,
                        enterpriseTweakedGap,
                        quote(enterpriseHealth)));
            }
        } finally { // Do everything possible to not lose the results.
            var filename = "%s-%s.csv"
                    .formatted(getLibraryName(), DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
            var target = Path.of("results", filename);
            target.getParent().toFile().mkdirs();
            Files.writeString(target, result);
            LOGGER.info("Wrote results to {}.", target);
        }
    }

    private static String roundToOneDecimal(BigDecimal d) {
        return roundToOneDecimal(d.doubleValue());
    }

    private static String roundToOneDecimal(double d) {
        return String.format("%.1f", d);
    }

    private static String quote(Object s) {
        return "\"" + s + "\"";
    }

    private Map<Dataset_, Result<Dataset_, Score_>> run(Configuration_ configuration, Dataset_... datasets)
            throws ExecutionException, InterruptedException {
        System.out.println("Running with " + configuration.name() + " solver config");
        var results = new TreeMap<Dataset_, Result<Dataset_, Score_>>();
        var parallelSolverCount = determineParallelSolverCount(configuration);
        try (var executorService = Executors.newFixedThreadPool(parallelSolverCount)) {
            var resultFutureList = new ArrayList<Future<Result<Dataset_, Score_>>>(datasets.length);
            for (var dataset : datasets) {
                var solverConfig = configuration.getSolverConfig(dataset);
                var future = executorService.submit(() -> solveDataset(configuration, dataset, solverConfig, datasets.length));
                resultFutureList.add(future);
            }
            for (var resultFuture : resultFutureList) {
                var result = resultFuture.get();
                results.put(result.dataset(), result);
            }
        }
        return results;
    }

    private int determineParallelSolverCount(Configuration_ configuration) {
        return configuration.usesEnterprise() ? MAX_THREADS / ENTERPRISE_MOVE_THREAD_COUNT : MAX_THREADS;
    }

    private BigDecimal computeGap(Dataset_ dataset, Score_ actual) {
        var bestKnownDistance = dataset.getBestKnownDistance();
        var actualDistance = extractDistance(dataset, actual);
        return actualDistance.subtract(bestKnownDistance)
                .divide(bestKnownDistance, 4, RoundingMode.HALF_EVEN);
    }

    private String determineHealth(Dataset_ dataset, InnerScore<Score_> actual, Duration runTime) {
        return determineHealth(dataset, actual, runTime, false);
    }

    private String determineHealth(Dataset_ dataset, InnerScore<Score_> actualInnerScore, Duration runTime, boolean addGap) {
        if (!actualInnerScore.isInitialized()) {
            return "Uninitialized.";
        }
        var actualScore = actualInnerScore.initialized();
        if (!actualScore.isFeasible()) {
            return "Infeasible.";
        }
        var bestKnownDistance = dataset.getBestKnownDistance();
        var actualDistance = extractDistance(dataset, actualScore);
        var comparison = actualDistance.compareTo(bestKnownDistance);
        if (comparison == 0) {
            return "Optimal.";
        } else if (comparison < 0 && dataset.isBestKnownDistanceOptimal()) {
            return "Suspicious (%s better than optimal)."
                    .formatted(roundToOneDecimal(bestKnownDistance.subtract(actualDistance).doubleValue()));
        } else {
            var cutoff = MAX_SECONDS * 1000 - 100; // Give some leeway before declaring flat line.
            var gapString = addGap ? (" " + getGapString(dataset, actualScore)) : "";
            if (runTime.toMillis() < cutoff) {
                var actualRunTime = (int) Math.round((runTime.toMillis() - (UNIMPROVED_SECONDS_TERMINATION * 1000)) / 1000.0);
                return "Flatlined after ~" + actualRunTime + " s." + gapString;
            } else {
                return "Healthy." + gapString;
            }
        }
    }

    private String getGapString(Dataset_ dataset, Score_ actual) {
        var gap = computeGap(dataset, actual);
        return "(Gap: %.1f %%)".formatted(gap.doubleValue() * 100);
    }

    private Result<Dataset_, Score_> solveDataset(Configuration_ configuration, Dataset_ dataset, SolverConfig solverConfig,
            int totalDatasetCount) {
        var importer = createImporter();
        var solution = importer.readSolution(dataset.getPath().toFile());
        var solverFactory = SolverFactory.<Solution_> create(solverConfig);
        var solver = solverFactory.buildSolver();
        var nanotime = System.nanoTime();
        var remainingDatasets = totalDatasetCount - dataset.ordinal();
        var parallelSolverCount = determineParallelSolverCount(configuration);
        var remainingCycles = (long) Math.ceil(remainingDatasets / (double) parallelSolverCount);
        var minutesRemaining = Duration.ofSeconds(MAX_SECONDS * remainingCycles)
                .toMinutes();
        LOGGER.info("Started {} ({} / {}), ~{} minute(s) remain in {}.", dataset.name(), dataset.ordinal() + 1,
                totalDatasetCount, minutesRemaining, configuration.name());
        var bestSolution = solver.solve(solution);
        var solutionDescriptor = ((DefaultSolverFactory<Solution_>) solverFactory).getSolutionDescriptor();
        var initializationStatistics = solutionDescriptor.computeInitializationStatistics(bestSolution);
        var actualDistance = extractScore(bestSolution);
        var innerScore = initializationStatistics.isInitialized() ? InnerScore.of(actualDistance)
                : InnerScore.ofUninitialized(actualDistance, initializationStatistics.getInitCount());
        var runtime = Duration.ofNanos(System.nanoTime() - nanotime);
        var health = determineHealth(dataset, innerScore, runtime, true);
        LOGGER.info("Solved {} in {} ms with a distance of {}; verdict: {}", dataset.name(), runtime.toMillis(),
                roundToOneDecimal(extractDistance(dataset, actualDistance)), health);
        return new Result<>(dataset, innerScore, countLocations(bestSolution) + 1, countVehicles(bestSolution), runtime);
    }

}
