package ai.timefold.solver.benchmarks.competitive.tsplib95;

import java.math.RoundingMode;

import ai.timefold.solver.benchmarks.competitive.AbstractCompetitiveBenchmark;
import ai.timefold.solver.benchmarks.competitive.Configuration;
import ai.timefold.solver.benchmarks.examples.tsp.domain.Tour;
import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;
import ai.timefold.solver.benchmarks.examples.tsp.domain.Visit;
import ai.timefold.solver.benchmarks.examples.tsp.domain.solver.nearby.VisitNearbyDistanceMeter;
import ai.timefold.solver.benchmarks.examples.tsp.score.TspConstraintProvider;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;

public enum TSPLIBConfiguration implements Configuration<TSPLIBDataset> {

    /**
     * Community edition, everything left on default.
     */
    COMMUNITY_EDITION(false),
    /**
     * Full power of the enterprise edition.
     */
    ENTERPRISE_EDITION(true);

    private final boolean usesEnterprise;

    TSPLIBConfiguration(boolean usesEnterprise) {
        this.usesEnterprise = usesEnterprise;
    }

    @Override
    public SolverConfig getSolverConfig(TSPLIBDataset dataset) {
        return switch (this) {
            case COMMUNITY_EDITION -> getCommunityEditionSolverConfig(dataset);
            case ENTERPRISE_EDITION -> getEnterpriseEditionSolverConfig(dataset);
        };
    }

    @Override
    public boolean usesEnterprise() {
        return usesEnterprise;
    }

    @Override
    public String entityLabel() {
        return "Vehicle";
    }

    @Override
    public String valueLabel() {
        return "Location";
    }

    private SolverConfig getCommunityEditionSolverConfig(TSPLIBDataset dataset) {
        var threshold = dataset.getBestKnownSolution().negate()
                .setScale(0, RoundingMode.HALF_EVEN);
        var terminationConfig = new TerminationConfig()
                .withSpentLimit(getMaximumDurationPerDataset())
                .withUnimprovedSecondsSpentLimit(AbstractCompetitiveBenchmark.UNIMPROVED_SECONDS_TERMINATION)
                .withBestScoreLimit(Long.toString(threshold.longValue()));
        return new SolverConfig()
                .withSolutionClass(TspSolution.class)
                .withEntityClasses(Tour.class, Visit.class)
                .withConstraintProviderClass(TspConstraintProvider.class)
                .withTerminationConfig(terminationConfig)
                .withPhases(new ConstructionHeuristicPhaseConfig(), new LocalSearchPhaseConfig());

    }

    private SolverConfig getEnterpriseEditionSolverConfig(TSPLIBDataset dataset) {
        // Inherit community config, add move thread count and nearby distance meter.
        return getCommunityEditionSolverConfig(dataset)
                .withMoveThreadCount(Integer.toString(AbstractCompetitiveBenchmark.ENTERPRISE_MOVE_THREAD_COUNT))
                .withNearbyDistanceMeterClass(VisitNearbyDistanceMeter.class);
    }

}
