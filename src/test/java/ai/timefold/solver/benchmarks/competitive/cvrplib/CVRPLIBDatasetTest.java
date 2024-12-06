package ai.timefold.solver.benchmarks.competitive.cvrplib;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import ai.timefold.solver.benchmarks.examples.vehiclerouting.persistence.VehicleRoutingImporter;
import ai.timefold.solver.core.api.solver.SolverFactory;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@Execution(ExecutionMode.CONCURRENT)
class CVRPLIBDatasetTest {

    @Disabled
    @ParameterizedTest
    @EnumSource(CVRPLIBDataset.class)
    void runConstructionHeuristics(CVRPLIBDataset dataset) {
        assumeFalse(dataset.isLarge(), "Skipping large dataset: " + dataset); // CH takes too long.
        var solution = new VehicleRoutingImporter().readSolution(dataset.getPath().toFile());
        assertThat(solution).isNotNull();

        var config = CVRPLIBConfiguration.COMMUNITY_EDITION_TWEAKED.getSolverConfig(dataset);
        var phases = config.getPhaseConfigList().subList(0, 1); // Keep only CH.
        config.setPhaseConfigList(phases);

        var solverFactory = SolverFactory.create(config);
        var solver = solverFactory.buildSolver();
        var bestSolution = solver.solve(solution);
        assertThat(bestSolution).isNotNull();
    }

}
