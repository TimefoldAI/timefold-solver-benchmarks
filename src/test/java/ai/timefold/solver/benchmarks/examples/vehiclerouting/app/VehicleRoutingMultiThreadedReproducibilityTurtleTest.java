package ai.timefold.solver.benchmarks.examples.vehiclerouting.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.stream.IntStream;

import ai.timefold.solver.benchmarks.examples.common.TestSystemProperties;
import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.VehicleRoutingSolution;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.persistence.VehicleRoutingSolutionFileIO;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * The idea is to verify one of the basic requirements of Multithreaded Solving - the reproducibility of results. After
 * a constant number of steps, every iteration must finish with the same score.
 */
@EnabledIfSystemProperty(named = TestSystemProperties.TURTLE_TEST_SELECTION, matches = "vehiclerouting|all")
class VehicleRoutingMultiThreadedReproducibilityTurtleTest {

    private static final int REPETITION_COUNT = 10;

    private static final int STEP_LIMIT = 5000;

    private static final String MOVE_THREAD_COUNT = "4";

    private static final String DATA_SET = "unsolved/cvrptw-100customers-A.json";

    private final VehicleRoutingApp vehicleRoutingApp =
            new VehicleRoutingApp();

    private VehicleRoutingSolution[] vehicleRoutingSolutions = new VehicleRoutingSolution[REPETITION_COUNT];

    private SolverFactory<VehicleRoutingSolution> solverFactory;

    @BeforeEach
    void createUninitializedSolutions() {
        final VehicleRoutingSolutionFileIO io = new VehicleRoutingSolutionFileIO();
        for (int i = 0; i < REPETITION_COUNT; i++) {
            File dataSetFile = new File(CommonApp.determineDataDir(vehicleRoutingApp.getDataDirName()), DATA_SET);
            VehicleRoutingSolution solution = io.read(dataSetFile);
            vehicleRoutingSolutions[i] = solution;
        }

        SolverConfig solverConfig =
                SolverConfig.createFromXmlResource(vehicleRoutingApp.getSolverConfigResource());
        solverConfig.withEnvironmentMode(EnvironmentMode.PHASE_ASSERT)
                .withMoveThreadCount(MOVE_THREAD_COUNT);
        solverConfig.getPhaseConfigList().forEach(phaseConfig -> {
            if (LocalSearchPhaseConfig.class.isAssignableFrom(phaseConfig.getClass())) {
                phaseConfig.setTerminationConfig(new TerminationConfig().withStepCountLimit(STEP_LIMIT));
            }
        });
        solverFactory = SolverFactory.create(solverConfig);
    }

    @Test
    void multiThreadedSolvingIsReproducible() {
        IntStream.range(0, REPETITION_COUNT).forEach(this::solveAndCompareWithPrevious);
    }

    private void solveAndCompareWithPrevious(final int iteration) {
        Solver<VehicleRoutingSolution> solver = solverFactory.buildSolver();
        VehicleRoutingSolution bestSolution = solver.solve(vehicleRoutingSolutions[iteration]);
        vehicleRoutingSolutions[iteration] = bestSolution;

        if (iteration > 0) {
            VehicleRoutingSolution previousBestSolution = vehicleRoutingSolutions[iteration - 1];
            assertThat(previousBestSolution.getScore()).isEqualTo(bestSolution.getScore());
        }
    }

}
