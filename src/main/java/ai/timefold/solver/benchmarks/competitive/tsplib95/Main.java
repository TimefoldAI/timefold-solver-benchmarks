package ai.timefold.solver.benchmarks.competitive.tsplib95;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;
import ai.timefold.solver.benchmarks.examples.tsp.persistence.TspImporter;
import ai.timefold.solver.core.api.score.buildin.simplelong.SimpleLongScore;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    static final long MAX_SECONDS = 60;

    private static final int MAX_THREADS = 4; // Set to the number of performance cores on your machine.
    static final int ENTERPRISE_MOVE_THREAD_COUNT = 4; // Recommended to divide MAX_THREADS without remainder.

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        var communityResultList = run(Configuration.COMMUNITY_EDITION);
        var communityTweakedResultList = run(Configuration.COMMUNITY_EDITION_TWEAKED);
        var enterpriseResultList = run(Configuration.ENTERPRISE_EDITION);

        String line = """
                %s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s
                """;
        String header = line.formatted("Dataset", "Size", "Best known score",
                "CE Achieved score", "CE run time (ms)", "CE gap to best (%)", "CE comment",
                "CE+ Achieved score", "CE+ run time (ms)", "CE+ gap to best (%)", "CE+ comment",
                "EE Achieved score", "EE run time (ms)", "EE gap to best (%)", "EE comment");

        var result = new StringBuilder()
                .append(header);
        for (var dataset : Dataset.values()) {
            var communityResult = communityResultList.get(dataset);
            var communityTweakedResult = communityTweakedResultList.get(dataset);
            var enterpriseResult = enterpriseResultList.get(dataset);

            var datasetName = dataset.name();
            var bestKnownDistance = dataset.getBestKnownDistance();
            var communityScore = communityResult.score();
            var communityRuntime = communityResult.runtime.toMillis();
            var communityGap = computeGap(bestKnownDistance, communityScore);
            var communityComment = getComment(bestKnownDistance, communityScore);
            var communityTweakedScore = communityTweakedResult.score();
            var communityTweakedRuntime = communityTweakedResult.runtime.toMillis();
            var communityTweakedGap = computeGap(bestKnownDistance, communityTweakedScore);
            var communityTweakedComment = getComment(bestKnownDistance, communityTweakedScore);
            var enterpriseScore = enterpriseResult.score();
            var enterpriseRuntime = enterpriseResult.runtime.toMillis();
            var enterpriseTweakedGap = computeGap(bestKnownDistance, enterpriseScore);
            var enterpriseComment = getComment(bestKnownDistance, enterpriseScore);
            result.append(line.formatted(
                    quote(datasetName),
                    communityResult.locationCount(),
                    bestKnownDistance,
                    -communityScore.score(),
                    communityRuntime,
                    communityGap,
                    quote(communityComment),
                    -communityTweakedScore.score(),
                    communityTweakedRuntime,
                    communityTweakedGap,
                    quote(communityTweakedComment),
                    -enterpriseScore.score(),
                    enterpriseRuntime,
                    enterpriseTweakedGap,
                    quote(enterpriseComment)));
        }

        var target = Path.of("results", DateTimeFormatter.ISO_INSTANT.format(Instant.now()) + ".csv");
        target.getParent().toFile().mkdirs();
        Files.writeString(target, result);
        LOGGER.info("Wrote results to {}.", target);
    }

    private static String quote(Object s) {
        return "\"" + s + "\"";
    }

    private static Map<Dataset, Result> run(Configuration configuration) throws ExecutionException, InterruptedException {
        System.out.println("Running with " + configuration.name() + " solver config");
        var results = new EnumMap<Dataset, Result>(Dataset.class);
        var parallelSolverCount =
                configuration == Configuration.ENTERPRISE_EDITION ? MAX_THREADS / ENTERPRISE_MOVE_THREAD_COUNT : MAX_THREADS;
        try (var executorService = Executors.newFixedThreadPool(parallelSolverCount)) {
            var resultFutureList = new ArrayList<Future<Result>>(Dataset.values().length);
            for (var dataset : Dataset.values()) {
                var solverConfig = configuration.getSolverConfig(dataset);
                var future = executorService.submit(() -> solveDataset(configuration, dataset, solverConfig));
                resultFutureList.add(future);
            }
            for (var resultFuture : resultFutureList) {
                var result = resultFuture.get();
                results.put(result.dataset, result);
            }
        }
        return results;
    }

    private static BigDecimal computeGap(long bestKnown, SimpleLongScore actual) {
        long actualScore = -actual.score();
        if (actualScore == bestKnown) {
            return BigDecimal.ZERO;
        }
        var difference = actualScore - bestKnown;
        return BigDecimal.valueOf(difference, 0)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(bestKnown, 0), 2, RoundingMode.HALF_EVEN);
    }

    private static String getComment(long bestKnown, SimpleLongScore actual) {
        if (!actual.isSolutionInitialized()) {
            return "Uninitialized.";
        }
        long actualScore = -actual.score();
        if (actualScore == bestKnown) {
            return "Optimal.";
        } else if (actualScore > bestKnown) {
            return "Timed out.";
        } else { // The best known solutions are optimal; this suggests score calculation issues.
            return "Suspicious.";
        }
    }

    private static Result solveDataset(Configuration configuration, Dataset dataset, SolverConfig solverConfig) {
        var importer = new TspImporter();
        var solution = importer.readSolution(dataset.getPath().toFile());
        var solverFactory = SolverFactory.<TspSolution> create(solverConfig);
        var solver = solverFactory.buildSolver();
        var nanotime = System.nanoTime();
        LOGGER.info("Started {} ({} / {}) using {}.", dataset.name(), dataset.ordinal() + 1, Dataset.values().length,
                configuration.name());
        var bestSolution = solver.solve(solution);
        var runtime = Duration.ofNanos(System.nanoTime() - nanotime);
        var bestKnownDistance = dataset.getBestKnownDistance();
        var actualScore = bestSolution.getScore();
        var verdict = getComment(bestKnownDistance, actualScore);
        LOGGER.info("Solved {} in {} ms with a distance of {}; verdict: {}",
                dataset.name(),
                runtime.toMillis(),
                -bestSolution.getScore().score(),
                verdict);
        return new Result(dataset, actualScore, bestSolution.getVisitList().size() + 1, runtime);
    }

    private record Result(Dataset dataset, SimpleLongScore score, int locationCount, Duration runtime) {
    }

}
