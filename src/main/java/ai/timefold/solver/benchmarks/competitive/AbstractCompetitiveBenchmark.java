package ai.timefold.solver.benchmarks.competitive;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCompetitiveBenchmark<Dataset_ extends Dataset<Dataset_>, Configuration_ extends Configuration<Dataset_>, Solution_, Score_ extends Score<Score_>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCompetitiveBenchmark.class);

    public static final long MAX_SECONDS = 60;
    public static final long UNIMPROVED_SECONDS_TERMINATION = MAX_SECONDS / 3;

    static final int MAX_THREADS = 15; // Set to the number of performance cores on your machine.
    // Recommended to divide MAX_THREADS without remainder.
    // Don't overdo it with move threads; it's not a silver bullet.
    public static final int ENTERPRISE_MOVE_THREAD_COUNT = 4;

    protected abstract String getLibraryName();

    protected abstract Score_ extractScore(Solution_ solution);

    protected abstract BigDecimal extractDistance(Dataset_ dataset, Score_ score);

    protected abstract int countLocations(Solution_ solution);

    protected abstract int countVehicles(Solution_ solution);

    protected abstract AbstractSolutionImporter<Solution_> createImporter();

    public void run(List<Configuration_> configurations, Dataset_... datasets)
            throws IOException, ExecutionException, InterruptedException {
        run(configurations, null, datasets);
    }

    public void run(List<Configuration_> configurations, Long seed, Dataset_... datasets)
            throws ExecutionException, InterruptedException, IOException {

        var resultList = new ArrayList<Map<Dataset_, Result<Dataset_, Score_>>>(configurations.size());
        for (Configuration_ configuration : configurations) {
            resultList.add(run(configuration, seed, datasets));
        }
        var result = new StringBuilder();
        try {
            StringBuilder header = new StringBuilder("Dataset;Location count;Vehicle count;Best known score;");
            for (Configuration_ configuration : configurations) {
                header.append("%s Achieved score; %s run time (ms); %s gap to best (%%); %s Health;"
                        .formatted(configuration.name(), configuration.name(), configuration.name(), configuration.name()));
            }
            result.append(header)
                    .append("\n");

            for (var dataset : datasets) {
                var datasetName = dataset.name();
                StringBuilder line = new StringBuilder();
                line.append(quote(datasetName))
                        .append(";")
                        .append(resultList.get(0).get(dataset).locationCount())
                        .append(";")
                        .append(resultList.get(0).get(dataset).vehicleCount())
                        .append(";")
                        .append(roundToOneDecimal(dataset.getBestKnownDistance()))
                        .append(";");
                for (var configurationResult : resultList) {
                    var singleResult = configurationResult.get(dataset);
                    var score = singleResult.score();
                    var runtime = singleResult.runtime().toMillis();
                    var gap = computeGap(dataset, score);
                    var health = determineHealth(dataset, score, singleResult.runtime());
                    line.append(roundToOneDecimal(extractDistance(dataset, score)))
                            .append(";")
                            .append(runtime)
                            .append(";")
                            .append(gap)
                            .append(";")
                            .append(quote(health))
                            .append(";");
                }
                line.append("\n");
                result.append(line);
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

    private Map<Dataset_, Result<Dataset_, Score_>> run(Configuration_ configuration, Long seed, Dataset_... datasets)
            throws ExecutionException, InterruptedException {
        System.out.println(
                "Running with " + configuration.name() + " solver config with seed " + Optional.ofNullable(seed).orElse(0L));
        var results = new TreeMap<Dataset_, Result<Dataset_, Score_>>();
        var parallelSolverCount = determineParallelSolverCount(configuration);
        try (var executorService = Executors.newFixedThreadPool(parallelSolverCount)) {
            var resultFutureList = new ArrayList<Future<Result<Dataset_, Score_>>>(datasets.length);
            for (var dataset : datasets) {
                var solverConfig = configuration.getSolverConfig(dataset);
                if (seed != null) {
                    solverConfig.setRandomSeed(seed);
                }
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
        return configuration.usesEnterprise() ? MAX_THREADS / (ENTERPRISE_MOVE_THREAD_COUNT + 1) : MAX_THREADS;
    }

    private BigDecimal computeGap(Dataset_ dataset, Score_ actual) {
        var bestKnownDistance = dataset.getBestKnownDistance();
        var actualDistance = extractDistance(dataset, actual);
        return actualDistance.subtract(bestKnownDistance)
                .divide(bestKnownDistance, 4, RoundingMode.HALF_EVEN);
    }

    private String determineHealth(Dataset_ dataset, Score_ actual, Duration runTime) {
        return determineHealth(dataset, actual, runTime, false);
    }

    private String determineHealth(Dataset_ dataset, Score_ actual, Duration runTime, boolean addGap) {
        if (!actual.isSolutionInitialized()) {
            return "Uninitialized.";
        } else if (!actual.isFeasible()) {
            return "Infeasible.";
        }
        var bestKnownDistance = dataset.getBestKnownDistance();
        var actualDistance = extractDistance(dataset, actual);
        var comparison = actualDistance.compareTo(bestKnownDistance);
        if (comparison == 0) {
            return "Optimal.";
        } else if (comparison < 0 && dataset.isBestKnownDistanceOptimal()) {
            return "Suspicious (%s better than optimal)."
                    .formatted(roundToOneDecimal(bestKnownDistance.subtract(actualDistance).doubleValue()));
        } else {
            var cutoff = MAX_SECONDS * 1000 - 100; // Give some leeway before declaring flat line.
            var gapString = addGap ? (" " + getGapString(dataset, actual)) : "";
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
        var solution = readInputFile(dataset.getPath().toFile());
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
        var runtime = Duration.ofNanos(System.nanoTime() - nanotime);
        var actualDistance = extractScore(bestSolution);
        var health = determineHealth(dataset, actualDistance, runtime, true);
        LOGGER.info("Solved {} in {} ms with a distance of {}; verdict: {}", dataset.name(), runtime.toMillis(),
                roundToOneDecimal(extractDistance(dataset, actualDistance)), health);
        return new Result<>(dataset, actualDistance, countLocations(bestSolution) + 1, countVehicles(bestSolution), runtime);
    }

    public Solution_ readInputFile(File inputFile) {
        var importer = createImporter();
        return importer.readSolution(inputFile);
    }

}
