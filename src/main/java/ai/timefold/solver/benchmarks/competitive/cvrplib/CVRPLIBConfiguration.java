package ai.timefold.solver.benchmarks.competitive.cvrplib;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import ai.timefold.solver.benchmarks.competitive.AbstractCompetitiveBenchmark;
import ai.timefold.solver.benchmarks.competitive.Configuration;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Customer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Vehicle;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.VehicleRoutingSolution;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.location.AirLocation;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.solver.nearby.CustomerNearbyDistanceMeter;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.timewindowed.TimeWindowedCustomer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.score.VehicleRoutingConstraintProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.localsearch.decider.acceptor.AcceptorType;
import ai.timefold.solver.core.config.localsearch.decider.acceptor.LocalSearchAcceptorConfig;
import ai.timefold.solver.core.config.solver.PreviewFeature;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;

public enum CVRPLIBConfiguration implements Configuration<CVRPLIBDataset> {

    /**
     * Community edition, everything left on default.
     */
    COMMUNITY_EDITION(false),
    COMMUNITY_RECONFIGURATION_EDITION(false),
    /**
     * Community edition, everything left on default.
     */
    DLAS_RECONFIGURATION_EDITION(false),
    /**
     * Full power of the enterprise edition.
     */
    ENTERPRISE_EDITION(true);

    private final boolean usesEnterprise;

    CVRPLIBConfiguration(boolean usesEnterprise) {
        this.usesEnterprise = usesEnterprise;
    }

    @Override
    public SolverConfig getSolverConfig(CVRPLIBDataset dataset) {
        return switch (this) {
            case COMMUNITY_EDITION -> getCommunityEditionSolverConfig(dataset);
            case COMMUNITY_RECONFIGURATION_EDITION -> getCommunityReconfigurationEditionSolverConfig(dataset);
            case DLAS_RECONFIGURATION_EDITION -> getDLASCommunityEditionSolverConfig(dataset);
            case ENTERPRISE_EDITION -> getEnterpriseEditionSolverConfig(dataset);
        };
    }

    @Override
    public boolean usesEnterprise() {
        return usesEnterprise;
    }

    private static SolverConfig getCommunityEditionSolverConfig(CVRPLIBDataset dataset) {
        var threshold = dataset.getBestKnownDistance()
                .multiply(BigDecimal.valueOf(AirLocation.MULTIPLIER))
                .setScale(0, RoundingMode.HALF_EVEN)
                .negate();
        var terminationConfig = new TerminationConfig()
                .withSecondsSpentLimit(AbstractCompetitiveBenchmark.MAX_SECONDS)
                //                .withUnimprovedSecondsSpentLimit(AbstractCompetitiveBenchmark.UNIMPROVED_SECONDS_TERMINATION)
                .withBestScoreLimit(HardSoftLongScore.ofSoft(threshold.longValue()).toString());
        return new SolverConfig()
                .withSolutionClass(VehicleRoutingSolution.class)
                .withEntityClasses(Vehicle.class, Customer.class, TimeWindowedCustomer.class)
                .withConstraintProviderClass(VehicleRoutingConstraintProvider.class)
                .withTerminationConfig(terminationConfig)
                .withPhases(new ConstructionHeuristicPhaseConfig(), new LocalSearchPhaseConfig());

    }

    private static SolverConfig getCommunityReconfigurationEditionSolverConfig(CVRPLIBDataset dataset) {
        var threshold = dataset.getBestKnownDistance()
                .multiply(BigDecimal.valueOf(AirLocation.MULTIPLIER))
                .setScale(0, RoundingMode.HALF_EVEN)
                .negate();
        var terminationConfig = new TerminationConfig()
                .withSecondsSpentLimit(AbstractCompetitiveBenchmark.MAX_SECONDS)
                //                .withUnimprovedSecondsSpentLimit(AbstractCompetitiveBenchmark.UNIMPROVED_SECONDS_TERMINATION)
                .withBestScoreLimit(HardSoftLongScore.ofSoft(threshold.longValue()).toString());
        return new SolverConfig()
                .withPreviewFeature(PreviewFeature.ACCEPTOR_RECONFIGURATION)
                .withSolutionClass(VehicleRoutingSolution.class)
                .withEntityClasses(Vehicle.class, Customer.class, TimeWindowedCustomer.class)
                .withConstraintProviderClass(VehicleRoutingConstraintProvider.class)
                .withTerminationConfig(terminationConfig)
                .withPhases(new ConstructionHeuristicPhaseConfig(),
                        new LocalSearchPhaseConfig()
                                .withAcceptorConfig(new LocalSearchAcceptorConfig()
                                        .withAcceptorTypeList(List.of(AcceptorType.LATE_ACCEPTANCE))
                                        .withEnableReconfiguration(true)));

    }

    private static SolverConfig getDLASCommunityEditionSolverConfig(CVRPLIBDataset dataset) {
        var threshold = dataset.getBestKnownDistance()
                .multiply(BigDecimal.valueOf(AirLocation.MULTIPLIER))
                .setScale(0, RoundingMode.HALF_EVEN)
                .negate();
        var terminationConfig = new TerminationConfig()
                .withSecondsSpentLimit(AbstractCompetitiveBenchmark.MAX_SECONDS)
                //                .withUnimprovedSecondsSpentLimit(AbstractCompetitiveBenchmark.UNIMPROVED_SECONDS_TERMINATION)
                .withBestScoreLimit(HardSoftLongScore.ofSoft(threshold.longValue()).toString());
        return new SolverConfig()
                .withPreviewFeature(PreviewFeature.DIVERSIFIED_LATE_ACCEPTANCE, PreviewFeature.ACCEPTOR_RECONFIGURATION)
                .withSolutionClass(VehicleRoutingSolution.class)
                .withEntityClasses(Vehicle.class, Customer.class, TimeWindowedCustomer.class)
                .withConstraintProviderClass(VehicleRoutingConstraintProvider.class)
                .withTerminationConfig(terminationConfig)
                .withPhases(new ConstructionHeuristicPhaseConfig(),
                        new LocalSearchPhaseConfig()
                                .withAcceptorConfig(new LocalSearchAcceptorConfig()
                                        .withAcceptorTypeList(List.of(AcceptorType.DIVERSIFIED_LATE_ACCEPTANCE))
                                        .withEnableReconfiguration(true)));

    }

    private static SolverConfig getEnterpriseEditionSolverConfig(CVRPLIBDataset dataset) {
        // Inherit community config, add move thread count and nearby distance meter class.
        return getCommunityEditionSolverConfig(dataset)
                .withMoveThreadCount(Integer.toString(AbstractCompetitiveBenchmark.ENTERPRISE_MOVE_THREAD_COUNT))
                .withNearbyDistanceMeterClass(CustomerNearbyDistanceMeter.class);
    }
}
