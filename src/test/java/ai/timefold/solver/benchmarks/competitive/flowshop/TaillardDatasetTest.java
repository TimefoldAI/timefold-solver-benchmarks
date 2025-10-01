package ai.timefold.solver.benchmarks.competitive.flowshop;

import static org.assertj.core.api.Assertions.assertThat;

import ai.timefold.solver.benchmarks.examples.flowshop.persistence.TaillardImporter;
import ai.timefold.solver.core.api.solver.SolverFactory;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@Execution(ExecutionMode.CONCURRENT)
class TaillardDatasetTest {

    @ParameterizedTest
    @EnumSource(FlowShopDataset.class)
    void runConstructionHeuristics(FlowShopDataset dataset) {
        var solution = new TaillardImporter().readSolution(dataset.getPath().toFile());
        assertThat(solution).isNotNull();

        var config = FlowShopConfiguration.ENTERPRISE_EDITION.getSolverConfig(dataset);
        var phases = config.getPhaseConfigList().subList(0, 1); // Keep only CH.
        config.setPhaseConfigList(phases);
        config.setMoveThreadCount("1"); // So that the tests can efficiently run in parallel, while still being MT.

        var solverFactory = SolverFactory.create(config);
        var solver = solverFactory.buildSolver();
        var bestSolution = solver.solve(solution);
        assertThat(bestSolution).isNotNull();
    }

}
