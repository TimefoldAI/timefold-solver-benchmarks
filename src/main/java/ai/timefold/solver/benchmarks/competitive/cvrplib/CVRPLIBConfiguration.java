package ai.timefold.solver.benchmarks.competitive.cvrplib;

import ai.timefold.solver.benchmarks.competitive.AbstractCompetitiveBenchmark;
import ai.timefold.solver.benchmarks.competitive.Configuration;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Customer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Vehicle;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.VehicleRoutingSolution;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.solver.nearby.CustomerNearbyDistanceMeter;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.timewindowed.TimeWindowedCustomer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.score.VehicleRoutingConstraintProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.heuristic.selector.list.SubListSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.composite.UnionMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.SwapMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.list.SubListChangeMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.list.SubListSwapMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.list.kopt.KOptListMoveSelectorConfig;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;

public enum CVRPLIBConfiguration implements Configuration<CVRPLIBDataset> {

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

    @Override
    public SolverConfig getSolverConfig(CVRPLIBDataset dataset) {
        return switch (this) {
            case COMMUNITY_EDITION -> getCommunityEditionSolverConfig(dataset);
            case COMMUNITY_EDITION_TWEAKED -> getCommunityEditionTweakedSolverConfig(dataset);
            case ENTERPRISE_EDITION -> getEnterpriseEditionSolverConfig(dataset);
        };
    }

    private static SolverConfig getCommunityEditionSolverConfig(CVRPLIBDataset dataset) {
        var terminationConfig = new TerminationConfig()
                .withSecondsSpentLimit(AbstractCompetitiveBenchmark.MAX_SECONDS)
                .withBestScoreLimit(HardSoftLongScore.ofSoft(-dataset.getBestKnownDistance()).toString());
        return new SolverConfig()
                .withSolutionClass(VehicleRoutingSolution.class)
                .withEntityClasses(Vehicle.class, Customer.class, TimeWindowedCustomer.class)
                .withConstraintProviderClass(VehicleRoutingConstraintProvider.class)
                .withTerminationConfig(terminationConfig)
                .withPhases(new ConstructionHeuristicPhaseConfig(), new LocalSearchPhaseConfig());

    }

    private static SolverConfig getCommunityEditionTweakedSolverConfig(CVRPLIBDataset dataset) {
        return getCommunityEditionSolverConfig(dataset)
                .withPhases(new ConstructionHeuristicPhaseConfig(),
                        new LocalSearchPhaseConfig()
                                .withMoveSelectorConfig(new UnionMoveSelectorConfig()
                                        .withMoveSelectors(
                                                new ChangeMoveSelectorConfig(),
                                                new SwapMoveSelectorConfig(),
                                                new SubListChangeMoveSelectorConfig()
                                                        .withSubListSelectorConfig(
                                                                new SubListSelectorConfig().withMaximumSubListSize(50))
                                                        .withSelectReversingMoveToo(true),
                                                new SubListSwapMoveSelectorConfig()
                                                        .withSubListSelectorConfig(
                                                                new SubListSelectorConfig().withMaximumSubListSize(50))
                                                        .withSelectReversingMoveToo(true),
                                                new KOptListMoveSelectorConfig())));
    }

    private static SolverConfig getEnterpriseEditionSolverConfig(CVRPLIBDataset dataset) {
        // Inherit community config, add move thread count and nearby distance meter class.
        return getCommunityEditionTweakedSolverConfig(dataset)
                .withMoveThreadCount(Integer.toString(AbstractCompetitiveBenchmark.ENTERPRISE_MOVE_THREAD_COUNT))
                .withNearbyDistanceMeterClass(CustomerNearbyDistanceMeter.class);
    }

}
