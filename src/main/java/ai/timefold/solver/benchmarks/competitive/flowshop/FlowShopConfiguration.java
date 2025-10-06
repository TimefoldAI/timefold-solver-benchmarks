package ai.timefold.solver.benchmarks.competitive.flowshop;

import java.time.Duration;
import java.util.List;

import ai.timefold.solver.benchmarks.competitive.AbstractCompetitiveBenchmark;
import ai.timefold.solver.benchmarks.competitive.Configuration;
import ai.timefold.solver.benchmarks.examples.flowshop.domain.Job;
import ai.timefold.solver.benchmarks.examples.flowshop.domain.JobScheduleSolution;
import ai.timefold.solver.benchmarks.examples.flowshop.domain.Machine;
import ai.timefold.solver.benchmarks.examples.flowshop.phase.NEHCustomPhase;
import ai.timefold.solver.benchmarks.examples.flowshop.score.FlowShopConstraintProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.phase.PhaseConfig;
import ai.timefold.solver.core.config.phase.custom.CustomPhaseConfig;
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
    public Duration getMaximumDurationPerDataset() {
        return Duration.ofSeconds(10);
    }

    @Override
    public boolean usesEnterprise() {
        return usesEnterprise;
    }

    @Override
    public String entityLabel() {
        return "Machine";
    }

    @Override
    public String valueLabel() {
        return "Job";
    }

    private SolverConfig getCommunityEditionSolverConfig(FlowShopDataset dataset) {
        var threshold = dataset.getBestKnownSolution()
                .negate();
        var terminationConfig = new TerminationConfig()
                .withSpentLimit(getMaximumDurationPerDataset())
                .withBestScoreLimit(HardSoftLongScore.ofSoft(threshold.longValue()).toString());
        var phasesList =
                List.<PhaseConfig> of(new CustomPhaseConfig().withCustomPhaseCommandList(List.of(new NEHCustomPhase())),
                        new LocalSearchPhaseConfig());
        return new SolverConfig()
                .withSolutionClass(JobScheduleSolution.class)
                .withEntityClasses(Machine.class, Job.class)
                .withConstraintProviderClass(FlowShopConstraintProvider.class)
                .withTerminationConfig(terminationConfig)
                .withPhaseList(phasesList);

    }

    private SolverConfig getEnterpriseEditionSolverConfig(FlowShopDataset dataset) {
        // Inherit community config, add move thread count
        return getCommunityEditionSolverConfig(dataset)
                .withMoveThreadCount(Integer.toString(AbstractCompetitiveBenchmark.ENTERPRISE_MOVE_THREAD_COUNT));
    }

}
