package ai.timefold.solver.benchmarks.competitive.tsplib95;

import java.math.RoundingMode;

import ai.timefold.solver.benchmarks.competitive.AbstractCompetitiveBenchmark;
import ai.timefold.solver.benchmarks.competitive.Configuration;
import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;
import ai.timefold.solver.benchmarks.examples.tsp.domain.Visit;
import ai.timefold.solver.benchmarks.examples.tsp.domain.solver.nearby.VisitNearbyDistanceMeter;
import ai.timefold.solver.benchmarks.examples.tsp.score.TspConstraintProvider;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicType;
import ai.timefold.solver.core.config.heuristic.selector.move.composite.UnionMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.SwapMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.chained.SubChainChangeMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.chained.SubChainSwapMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.chained.TailChainSwapMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.value.chained.SubChainSelectorConfig;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;

public enum TSPLIBConfiguration implements Configuration<TSPLIBDataset> {

    /**
     * Community edition, everything left on default.
     */
    COMMUNITY_EDITION(false),
    /**
     * Community edition, added some move selectors.
     */
    COMMUNITY_EDITION_TWEAKED(false),
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
            case COMMUNITY_EDITION_TWEAKED -> getCommunityEditionTweakedSolverConfig(dataset);
            case ENTERPRISE_EDITION -> getEnterpriseEditionSolverConfig(dataset);
        };
    }

    @Override
    public boolean usesEnterprise() {
        return usesEnterprise;
    }

    private static SolverConfig getCommunityEditionSolverConfig(TSPLIBDataset dataset) {
        var threshold = dataset.getBestKnownDistance().negate()
                .setScale(0, RoundingMode.HALF_EVEN);
        var terminationConfig = new TerminationConfig()
                .withSecondsSpentLimit(AbstractCompetitiveBenchmark.MAX_SECONDS)
                .withUnimprovedSecondsSpentLimit(AbstractCompetitiveBenchmark.UNIMPROVED_SECONDS_TERMINATION)
                .withBestScoreLimit(Long.toString(threshold.longValue()));
        return new SolverConfig()
                .withSolutionClass(TspSolution.class)
                .withEntityClasses(Visit.class)
                .withConstraintProviderClass(TspConstraintProvider.class)
                .withTerminationConfig(terminationConfig)
                .withPhases(new ConstructionHeuristicPhaseConfig(), new LocalSearchPhaseConfig());

    }

    private static SolverConfig getCommunityEditionTweakedSolverConfig(TSPLIBDataset dataset) {
        return getCommunityEditionSolverConfig(dataset)
                .withPhases(
                        new ConstructionHeuristicPhaseConfig()
                                .withConstructionHeuristicType(ConstructionHeuristicType.FIRST_FIT_DECREASING),
                        new LocalSearchPhaseConfig()
                                .withMoveSelectorConfig(new UnionMoveSelectorConfig()
                                        .withMoveSelectors(
                                                new ChangeMoveSelectorConfig(),
                                                new SwapMoveSelectorConfig(),
                                                new SubChainChangeMoveSelectorConfig()
                                                        .withSubChainSelectorConfig(
                                                                new SubChainSelectorConfig().withMaximumSubChainSize(50))
                                                        .withSelectReversingMoveToo(true),
                                                new SubChainSwapMoveSelectorConfig()
                                                        .withSubChainSelectorConfig(
                                                                new SubChainSelectorConfig().withMaximumSubChainSize(50))
                                                        .withSelectReversingMoveToo(true),
                                                new TailChainSwapMoveSelectorConfig())));
    }

    private static SolverConfig getEnterpriseEditionSolverConfig(TSPLIBDataset dataset) {
        // Inherit community config, add move thread count and nearby distance meter.
        return getCommunityEditionTweakedSolverConfig(dataset)
                .withMoveThreadCount(Integer.toString(AbstractCompetitiveBenchmark.ENTERPRISE_MOVE_THREAD_COUNT))
                .withNearbyDistanceMeterClass(VisitNearbyDistanceMeter.class);
    }

}
