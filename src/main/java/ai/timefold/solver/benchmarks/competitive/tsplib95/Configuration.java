package ai.timefold.solver.benchmarks.competitive.tsplib95;

import java.util.ArrayList;

import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;
import ai.timefold.solver.benchmarks.examples.tsp.domain.Visit;
import ai.timefold.solver.benchmarks.examples.tsp.domain.solver.nearby.VisitNearbyDistanceMeter;
import ai.timefold.solver.benchmarks.examples.tsp.score.TspConstraintProvider;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicType;
import ai.timefold.solver.core.config.heuristic.selector.common.nearby.NearbySelectionConfig;
import ai.timefold.solver.core.config.heuristic.selector.entity.EntitySelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.composite.UnionMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.SwapMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.chained.SubChainChangeMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.chained.SubChainSwapMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.chained.TailChainSwapMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.value.ValueSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.value.chained.SubChainSelectorConfig;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;

public enum Configuration {

    /**
     * Community edition, everything left on default.
     */
    COMMUNITY_EDITION,
    /**
     * Community edition, added some move selectors.
     */
    COMMUNITY_EDITION_TWEAKED,
    /**
     * Full power of the enterprise edition.
     */
    ENTERPRISE_EDITION;

    public SolverConfig getSolverConfig(Dataset dataset) {
        return switch (this) {
            case COMMUNITY_EDITION -> getCommunityEditionSolverConfig(dataset);
            case COMMUNITY_EDITION_TWEAKED -> getCommunityEditionTweakedSolverConfig(dataset);
            case ENTERPRISE_EDITION -> getEnterpriseEditionSolverConfig(dataset);
        };
    }

    private static SolverConfig getCommunityEditionSolverConfig(Dataset dataset) {
        var terminationConfig = new TerminationConfig()
                .withSecondsSpentLimit(Main.MAX_SECONDS)
                .withBestScoreLimit(Long.toString(-dataset.getBestKnownDistance()));
        return new SolverConfig()
                .withSolutionClass(TspSolution.class)
                .withEntityClasses(Visit.class)
                .withConstraintProviderClass(TspConstraintProvider.class)
                .withTerminationConfig(terminationConfig)
                .withPhases(new ConstructionHeuristicPhaseConfig(), new LocalSearchPhaseConfig());

    }

    private static SolverConfig getCommunityEditionTweakedSolverConfig(Dataset dataset) {
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

    private static SolverConfig getEnterpriseEditionSolverConfig(Dataset dataset) {
        // Inherit community config, add move thread count.
        var config = getCommunityEditionTweakedSolverConfig(dataset)
                .withMoveThreadCount(Integer.toString(Main.ENTERPRISE_MOVE_THREAD_COUNT));
        // Inherit construction heuristic.
        var constructionHeuristicPhaseConfig = (ConstructionHeuristicPhaseConfig) config.getPhaseConfigList().get(0);
        // Inherit local search, but add nearby selection.
        var localSearchPhaseConfig = (LocalSearchPhaseConfig) config.getPhaseConfigList().get(1);
        var moveSelectorConfig = (UnionMoveSelectorConfig) localSearchPhaseConfig.getMoveSelectorConfig();
        var moveSelectorConfigList = new ArrayList<>(moveSelectorConfig.getMoveSelectorList());
        moveSelectorConfigList.add(new ChangeMoveSelectorConfig()
                .withEntitySelectorConfig(new EntitySelectorConfig().withId("es1"))
                .withValueSelectorConfig(new ValueSelectorConfig()
                        .withNearbySelectionConfig(
                                new NearbySelectionConfig()
                                        .withOriginEntitySelectorConfig(new EntitySelectorConfig().withMimicSelectorRef("es1"))
                                        .withNearbyDistanceMeterClass(VisitNearbyDistanceMeter.class)
                                        .withParabolicDistributionSizeMaximum(40))));
        moveSelectorConfigList.add(new SwapMoveSelectorConfig()
                .withEntitySelectorConfig(new EntitySelectorConfig().withId("es2"))
                .withSecondaryEntitySelectorConfig(new EntitySelectorConfig()
                        .withNearbySelectionConfig(
                                new NearbySelectionConfig()
                                        .withOriginEntitySelectorConfig(new EntitySelectorConfig().withMimicSelectorRef("es2"))
                                        .withNearbyDistanceMeterClass(VisitNearbyDistanceMeter.class)
                                        .withParabolicDistributionSizeMaximum(40))));
        moveSelectorConfigList.add(new TailChainSwapMoveSelectorConfig()
                .withEntitySelectorConfig(new EntitySelectorConfig().withId("es3"))
                .withValueSelectorConfig(new ValueSelectorConfig()
                        .withNearbySelectionConfig(
                                new NearbySelectionConfig()
                                        .withOriginEntitySelectorConfig(new EntitySelectorConfig().withMimicSelectorRef("es3"))
                                        .withNearbyDistanceMeterClass(VisitNearbyDistanceMeter.class)
                                        .withParabolicDistributionSizeMaximum(40))));
        // Put it all together.
        return config.withPhases(
                constructionHeuristicPhaseConfig,
                new LocalSearchPhaseConfig()
                        .withMoveSelectorConfig(new UnionMoveSelectorConfig()
                                .withMoveSelectorList(moveSelectorConfigList)));
    }

}
