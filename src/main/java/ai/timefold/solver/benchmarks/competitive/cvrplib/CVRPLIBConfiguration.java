package ai.timefold.solver.benchmarks.competitive.cvrplib;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;

public enum CVRPLIBConfiguration implements Configuration<CVRPLIBDataset> {

    /**
     * Community edition, everything left on default.
     */
    COMMUNITY_EDITION(false),
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
            case ENTERPRISE_EDITION -> getEnterpriseEditionSolverConfig(dataset);
        };
    }

    @Override
    public boolean usesEnterprise() {
        return usesEnterprise;
    }

    private SolverConfig getCommunityEditionSolverConfig(CVRPLIBDataset dataset) {
        var threshold = dataset.getBestKnownSolution()
                .multiply(BigDecimal.valueOf(AirLocation.MULTIPLIER))
                .setScale(0, RoundingMode.HALF_EVEN)
                .negate();
        var terminationConfig = new TerminationConfig()
                .withSpentLimit(getMaximumDurationPerDataset())
                .withUnimprovedSecondsSpentLimit(AbstractCompetitiveBenchmark.UNIMPROVED_SECONDS_TERMINATION)
                .withBestScoreLimit(HardSoftLongScore.ofSoft(threshold.longValue()).toString());
        return new SolverConfig()
                .withSolutionClass(VehicleRoutingSolution.class)
                .withEntityClasses(Vehicle.class, Customer.class, TimeWindowedCustomer.class)
                .withConstraintProviderClass(VehicleRoutingConstraintProvider.class)
                .withTerminationConfig(terminationConfig)
                .withPhases(new ConstructionHeuristicPhaseConfig(), new LocalSearchPhaseConfig());

    }

    private SolverConfig getEnterpriseEditionSolverConfig(CVRPLIBDataset dataset) {
        // Inherit community config, add move thread count and nearby distance meter class.
        return getCommunityEditionSolverConfig(dataset)
                .withMoveThreadCount(Integer.toString(AbstractCompetitiveBenchmark.ENTERPRISE_MOVE_THREAD_COUNT))
                .withNearbyDistanceMeterClass(CustomerNearbyDistanceMeter.class);
    }

}
