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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCompetitiveBenchmark<Dataset_ extends Dataset<Dataset_>, Configuration_ extends Configuration<Dataset_>, Solution_, Score_ extends Score<Score_>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCompetitiveBenchmark.class);

    public static final long MAX_SECONDS = 60;

    static final int MAX_THREADS = 4; // Set to the number of performance cores on your machine.
    public static final int ENTERPRISE_MOVE_THREAD_COUNT = 4; // Recommended to divide MAX_THREADS without remainder.

    protected abstract String getLibraryName();

    protected abstract Score_ extractScore(Solution_ solution);

    protected abstract long extractDistance(Score_ score);

    protected abstract int countLocations(Solution_ solution);

    protected abstract int countVehicles(Solution_ solution);

    protected abstract AbstractSolutionImporter<Solution_> createImporter();

    public void run(Configuration_ communityEdition, Configuration_ communityEditionTweaked, Configuration_ enterpriseEdition,
            Dataset_... datasets)
            throws ExecutionException, InterruptedException, IOException {
        var communityResultList = run(communityEdition, false, datasets);
        var communityTweakedResultList = run(communityEditionTweaked, false, datasets);
        var enterpriseResultList = run(enterpriseEdition, true, datasets);

        var result = new StringBuilder();
        try {
            String line = """
                    %s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s
                    """;
            String header = line.formatted("Dataset", "Location count", "Vehicle count", "Best known score",
                    "CE Achieved score", "CE run time (ms)", "CE gap to best (%)", "CE comment",
                    "CE+ Achieved score", "CE+ run time (ms)", "CE+ gap to best (%)", "CE+ comment",
                    "EE Achieved score", "EE run time (ms)", "EE gap to best (%)", "EE comment");
            result.append(header);

            for (var dataset : datasets) {
                var communityResult = communityResultList.get(dataset);
                var communityTweakedResult = communityTweakedResultList.get(dataset);
                var enterpriseResult = enterpriseResultList.get(dataset);

                var datasetName = dataset.name();
                var bestKnownDistance = dataset.getBestKnownDistance();
                var communityScore = communityResult.score();
                var communityRuntime = communityResult.runtime().toMillis();
                var communityGap = computeGap(bestKnownDistance, communityScore);
                var communityComment = getComment(bestKnownDistance, communityScore);
                var communityTweakedScore = communityTweakedResult.score();
                var communityTweakedRuntime = communityTweakedResult.runtime().toMillis();
                var communityTweakedGap = computeGap(bestKnownDistance, communityTweakedScore);
                var communityTweakedComment = getComment(bestKnownDistance, communityTweakedScore);
                var enterpriseScore = enterpriseResult.score();
                var enterpriseRuntime = enterpriseResult.runtime().toMillis();
                var enterpriseTweakedGap = computeGap(bestKnownDistance, enterpriseScore);
                var enterpriseComment = getComment(bestKnownDistance, enterpriseScore);
                result.append(line.formatted(
                        quote(datasetName),
                        communityResult.locationCount(),
                        communityResult.vehicleCount(),
                        bestKnownDistance,
                        extractDistance(communityScore),
                        communityRuntime,
                        communityGap,
                        quote(communityComment),
                        extractDistance(communityTweakedScore),
                        communityTweakedRuntime,
                        communityTweakedGap,
                        quote(communityTweakedComment),
                        extractDistance(enterpriseScore),
                        enterpriseRuntime,
                        enterpriseTweakedGap,
                        quote(enterpriseComment)));
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

    private static String quote(Object s) {
        return "\"" + s + "\"";
    }

    private Map<Dataset_, Result<Dataset_, Score_>> run(Configuration_ configuration, boolean isEnterprise,
            Dataset_... datasets)
            throws ExecutionException, InterruptedException {
        System.out.println("Running with " + configuration.name() + " solver config");
        var results = new TreeMap<Dataset_, Result<Dataset_, Score_>>();
        var parallelSolverCount = isEnterprise ? MAX_THREADS / ENTERPRISE_MOVE_THREAD_COUNT : MAX_THREADS;
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

    private BigDecimal computeGap(long bestKnown, Score_ actual) {
        long actualScore = extractDistance(actual);
        if (actualScore == bestKnown) {
            return BigDecimal.ZERO;
        }
        var difference = actualScore - bestKnown;
        return BigDecimal.valueOf(difference, 0)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(bestKnown, 0), 2, RoundingMode.HALF_EVEN);
    }

    private String getComment(long bestKnown, Score_ actual) {
        if (!actual.isSolutionInitialized()) {
            return "Uninitialized.";
        } else if (!actual.isFeasible()) {
            return "Infeasible.";
        }
        long actualScore = extractDistance(actual);
        if (actualScore == bestKnown) {
            return "Optimal.";
        } else if (actualScore == bestKnown - 1) {
            return "Optimal. (Rounding error.)"; // Happens in CVRPTW, which uses doubles.
        } else if (actualScore < bestKnown) {
            // The best known solutions are typically optimal; this suggests score calculation issues.
            return "Suspicious.";
        } else {
            return "Timed out.";
        }
    }

    private Result<Dataset_, Score_> solveDataset(Configuration_ configuration, Dataset_ dataset, SolverConfig solverConfig,
            int totalDatasetCount) {
        var importer = createImporter();
        var solution = importer.readSolution(dataset.getPath().toFile());
        var solverFactory = SolverFactory.<Solution_> create(solverConfig);
        var solver = solverFactory.buildSolver();
        var nanotime = System.nanoTime();
        LOGGER.info("Started {} ({} / {}) using {}.", dataset.name(), dataset.ordinal() + 1, totalDatasetCount,
                configuration.name());
        var bestSolution = solver.solve(solution);
        var runtime = Duration.ofNanos(System.nanoTime() - nanotime);
        var bestKnownDistance = dataset.getBestKnownDistance();
        var actualDistance = extractScore(bestSolution);
        var verdict = getComment(bestKnownDistance, actualDistance);
        LOGGER.info("Solved {} in {} ms with a distance of {}; verdict: {}",
                dataset.name(),
                runtime.toMillis(),
                extractDistance(actualDistance),
                verdict);
        return new Result<>(dataset, actualDistance, countLocations(bestSolution) + 1, countVehicles(bestSolution), runtime);
    }

}
