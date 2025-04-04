package ai.timefold.solver.benchmarks.examples.common.app;

import static ai.timefold.solver.core.config.solver.EnvironmentMode.FULL_ASSERT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.examples.common.TestSystemProperties;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.ScoreExplanation;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatchTotal;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Runs an example {@link Solver}.
 * <p>
 * A test should run in ~5 seconds, choose the bestScoreLimit accordingly.
 * Always use a {@link Timeout} on {@link Test}, preferably 10 minutes as some CI nodes are slow.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
@Execution(ExecutionMode.CONCURRENT)
public abstract class SolverSmokeTest<Solution_, Score_ extends Score<Score_>> extends LoggingTest {

    private static final String MOVE_THREAD_COUNTS_STRING = System.getProperty(TestSystemProperties.MOVE_THREAD_COUNTS);

    protected SolutionFileIO<Solution_> solutionFileIO;
    protected String solverConfigResource;

    private static Stream<String> moveThreadCounts() {
        return Optional.ofNullable(MOVE_THREAD_COUNTS_STRING)
                .map(s -> Arrays.stream(s.split(",")))
                .orElse(Stream.of(SolverConfig.MOVE_THREAD_COUNT_NONE));
    }

    @BeforeEach
    public void setUp() {
        CommonApp<Solution_> commonApp = createCommonApp();
        solutionFileIO = commonApp.createSolutionFileIO();
        solverConfigResource = commonApp.getSolverConfigResource();
    }

    @TestFactory
    @Execution(ExecutionMode.CONCURRENT)
    @Timeout(600)
    Stream<DynamicTest> runSpeedTest() {
        return moveThreadCounts()
                .flatMap(moveThreadCount -> testData()
                        .flatMap(testData -> {
                            Stream.Builder<DynamicTest> streamBuilder = Stream.builder();
                            streamBuilder.add(createSpeedTest(testData.unsolvedDataFile,
                                    EnvironmentMode.PHASE_ASSERT,
                                    testData.bestScoreLimitForReproducible, moveThreadCount));
                            if (testData.bestScoreLimitForFastAssert != null) {
                                streamBuilder.add(createSpeedTest(testData.unsolvedDataFile,
                                        EnvironmentMode.STEP_ASSERT,
                                        testData.bestScoreLimitForFastAssert, moveThreadCount));
                            }
                            if (testData.bestScoreLimitForFullAssert != null) {
                                streamBuilder.add(createSpeedTest(testData.unsolvedDataFile,
                                        FULL_ASSERT,
                                        testData.bestScoreLimitForFullAssert, moveThreadCount));
                            }
                            return streamBuilder.build();
                        }));
    }

    private DynamicTest createSpeedTest(String unsolvedDataFile, EnvironmentMode environmentMode, Score_ bestScoreLimit,
            String moveThreadCount) {
        String testName = unsolvedDataFile.replaceFirst(".*/", "")
                + ", "
                + environmentMode
                + ", threads: " + moveThreadCount;
        return dynamicTest(testName,
                () -> runSpeedTest(
                        new File(unsolvedDataFile),
                        bestScoreLimit,
                        environmentMode,
                        moveThreadCount));
    }

    protected abstract CommonApp<Solution_> createCommonApp();

    protected abstract Stream<TestData<Score_>> testData();

    private void runSpeedTest(File unsolvedDataFile, Score_ bestScoreLimit, EnvironmentMode environmentMode,
            String moveThreadCount) {
        SolverFactory<Solution_> solverFactory = buildSpeedSolverFactory(bestScoreLimit, environmentMode, moveThreadCount);
        Solution_ problem = solutionFileIO.read(unsolvedDataFile);
        logger.info("Opened: {}", unsolvedDataFile);
        Solver<Solution_> solver = solverFactory.buildSolver();
        Solution_ bestSolution = solver.solve(problem);
        assertScoreAndConstraintMatches(solverFactory, bestSolution, bestScoreLimit);
    }

    private SolverFactory<Solution_> buildSpeedSolverFactory(Score_ bestScoreLimit, EnvironmentMode environmentMode,
            String moveThreadCount) {
        SolverConfig solverConfig = SolverConfig.createFromXmlResource(solverConfigResource);
        solverConfig.withEnvironmentMode(environmentMode)
                .withTerminationConfig(new TerminationConfig()
                        .withBestScoreLimit(bestScoreLimit.toString()))
                .withMoveThreadCount(moveThreadCount);
        ScoreDirectorFactoryConfig scoreDirectorFactoryConfig =
                Objects.requireNonNullElseGet(solverConfig.getScoreDirectorFactoryConfig(),
                        ScoreDirectorFactoryConfig::new);
        if (scoreDirectorFactoryConfig.getConstraintProviderClass() == null) {
            Assertions.fail("Test does not support constraint streams.");
        }
        solverConfig.setScoreDirectorFactoryConfig(scoreDirectorFactoryConfig);
        return SolverFactory.create(solverConfig);
    }

    private void assertScoreAndConstraintMatches(SolverFactory<Solution_> solverFactory, Solution_ bestSolution,
            Score_ bestScoreLimit) {
        assertThat(bestSolution).isNotNull();
        SolutionManager<Solution_, Score_> solutionManager = SolutionManager.create(solverFactory);
        Score_ bestScore = solutionManager.update(bestSolution);
        assertThat(bestScore)
                .as("The bestScore (" + bestScore + ") must be at least the bestScoreLimit (" + bestScoreLimit + ").")
                .isGreaterThanOrEqualTo(bestScoreLimit);

        ScoreExplanation<Solution_, Score_> scoreExplanation = solutionManager.explain(bestSolution);
        Map<String, ConstraintMatchTotal<Score_>> constraintMatchTotals =
                scoreExplanation.getConstraintMatchTotalMap();
        assertThat(constraintMatchTotals).isNotNull();
        assertThat(constraintMatchTotals.values().stream()
                .map(ConstraintMatchTotal::getScore)
                .reduce(Score::add)
                .orElse(bestScore.zero()))
                .isEqualTo(scoreExplanation.getScore());
        assertThat(scoreExplanation.getIndictmentMap()).isNotNull();
    }

    protected static class TestData<Score_ extends Score<Score_>> {

        public static <Score_ extends Score<Score_>> TestData<Score_> of(String unsolvedDataFile,
                Score_ bestScoreLimitForReproducible) {
            return of(unsolvedDataFile, bestScoreLimitForReproducible, null);
        }

        public static <Score_ extends Score<Score_>> TestData<Score_> of(String unsolvedDataFile,
                Score_ bestScoreLimitForReproducible, Score_ bestScoreLimitForFastAssert) {
            return of(unsolvedDataFile, bestScoreLimitForReproducible,
                    bestScoreLimitForFastAssert, null);
        }

        public static <Score_ extends Score<Score_>> TestData<Score_> of(String unsolvedDataFile,
                Score_ bestScoreLimitForReproducible, Score_ bestScoreLimitForFastAssert, Score_ bestScoreLimitForFullAssert) {
            return new TestData<>(unsolvedDataFile, bestScoreLimitForReproducible,
                    bestScoreLimitForFastAssert, bestScoreLimitForFullAssert);
        }

        private final String unsolvedDataFile;
        private final Score_ bestScoreLimitForReproducible;
        private final Score_ bestScoreLimitForFastAssert;
        private final Score_ bestScoreLimitForFullAssert;

        private TestData(String unsolvedDataFile, Score_ bestScoreLimitForReproducible, Score_ bestScoreLimitForFastAssert,
                Score_ bestScoreLimitForFullAssert) {
            this.unsolvedDataFile = unsolvedDataFile;
            this.bestScoreLimitForReproducible = Objects.requireNonNull(bestScoreLimitForReproducible);
            this.bestScoreLimitForFastAssert = bestScoreLimitForFastAssert;
            this.bestScoreLimitForFullAssert = bestScoreLimitForFullAssert;
        }
    }
}
