package ai.timefold.solver.benchmarks.competitive.cvrplib;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import ai.timefold.solver.benchmarks.examples.vehiclerouting.persistence.VehicleRoutingImporter;
import ai.timefold.solver.core.api.solver.SolverFactory;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@Execution(ExecutionMode.CONCURRENT)
class CVRPLIBDatasetTest {

    @ParameterizedTest
    @EnumSource(CVRPLIBDataset.class)
    void runConstructionHeuristics(CVRPLIBDataset dataset) {
        assumeFalse(dataset.isLarge(), "Skipping large dataset: " + dataset); // CH takes too long.
        var solution = new VehicleRoutingImporter().readSolution(dataset.getPath().toFile());
        assertThat(solution).isNotNull();

        var config = CVRPLIBConfiguration.ENTERPRISE_EDITION.getSolverConfig(dataset);
        var phases = config.getPhaseConfigList().subList(0, 1); // Keep only CH.
        config.setPhaseConfigList(phases);
        config.setMoveThreadCount("1"); // So that the tests can efficiently run in parallel, while still being MT.

        var solverFactory = SolverFactory.create(config);
        var solver = solverFactory.buildSolver();
        var bestSolution = solver.solve(solution);
        assertThat(bestSolution).isNotNull();
    }

}
