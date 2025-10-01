package ai.timefold.solver.benchmarks.competitive.flowshop;

import ai.timefold.solver.benchmarks.competitive.AbstractCompetitiveBenchmark;
import ai.timefold.solver.benchmarks.competitive.Configuration;
import ai.timefold.solver.benchmarks.examples.flowshop.domain.Job;
import ai.timefold.solver.benchmarks.examples.flowshop.domain.JobScheduleSolution;
import ai.timefold.solver.benchmarks.examples.flowshop.domain.Machine;
import ai.timefold.solver.benchmarks.examples.flowshop.score.FlowShopConstraintProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;

public enum FlowShopConfiguration implements Configuration<FlowShopDataset> {

    /**
     * Community edition, everything left on default.
     */
    COMMUNITY_EDITION(false),
    /**
     * Full power of the enterprise edition.
     */
    ENTERPRISE_EDITION(true);

    private final boolean usesEnterprise;

    FlowShopConfiguration(boolean usesEnterprise) {
        this.usesEnterprise = usesEnterprise;
    }

    @Override
    public SolverConfig getSolverConfig(FlowShopDataset dataset) {
        return switch (this) {
            case COMMUNITY_EDITION -> getCommunityEditionSolverConfig(dataset);
            case ENTERPRISE_EDITION -> getEnterpriseEditionSolverConfig(dataset);
        };
    }

    @Override
    public boolean usesEnterprise() {
        return usesEnterprise;
    }

    private static SolverConfig getCommunityEditionSolverConfig(FlowShopDataset dataset) {
        var threshold = dataset.getBestKnownSolution()
                .negate();
        var terminationConfig = new TerminationConfig()
                .withSecondsSpentLimit(10L)
                .withBestScoreLimit(HardSoftLongScore.ofSoft(threshold.longValue()).toString());
        return new SolverConfig()
                .withSolutionClass(JobScheduleSolution.class)
                .withEntityClasses(Machine.class, Job.class)
                .withConstraintProviderClass(FlowShopConstraintProvider.class)
                .withTerminationConfig(terminationConfig)
                .withPhases(new ConstructionHeuristicPhaseConfig(), new LocalSearchPhaseConfig());

    }

    private static SolverConfig getEnterpriseEditionSolverConfig(FlowShopDataset dataset) {
        // Inherit community config, add move thread count
        return getCommunityEditionSolverConfig(dataset)
                .withMoveThreadCount(Integer.toString(AbstractCompetitiveBenchmark.ENTERPRISE_MOVE_THREAD_COUNT));
    }

}
