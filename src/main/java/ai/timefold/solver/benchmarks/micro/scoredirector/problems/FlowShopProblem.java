package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import ai.timefold.solver.benchmarks.examples.flowshop.domain.Job;
import ai.timefold.solver.benchmarks.examples.flowshop.domain.JobScheduleSolution;
import ai.timefold.solver.benchmarks.examples.flowshop.domain.Machine;
import ai.timefold.solver.benchmarks.examples.flowshop.persistence.FlowShopSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.flowshop.score.FlowShopConstraintProvider;
import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.api.domain.solution.SolutionFileIO;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;

public final class FlowShopProblem extends AbstractProblem<JobScheduleSolution> {

    public FlowShopProblem(ScoreDirectorType scoreDirectorType) {
        super(Example.FLOW_SHOP, scoreDirectorType);
    }

    private ScoreDirectorFactoryConfig buildScoreDirectorFactoryConfig(ScoreDirectorType scoreDirectorType) {
        var scoreDirectorFactoryConfig = buildInitialScoreDirectorFactoryConfig();
        return switch (scoreDirectorType) {
            case CONSTRAINT_STREAMS, CONSTRAINT_STREAMS_JUSTIFIED ->
                scoreDirectorFactoryConfig.withConstraintProviderClass(FlowShopConstraintProvider.class);
        };
    }

    @Override
    protected SolverConfig buildSolverConfig(ScoreDirectorType scoreDirectorType) {
        return new SolverConfig()
                .withSolutionClass(JobScheduleSolution.class)
                .withEntityClasses(Machine.class, Job.class)
                .withScoreDirectorFactory(buildScoreDirectorFactoryConfig(scoreDirectorType));
    }

    @Override
    protected SolutionFileIO<JobScheduleSolution> createSolutionFileIO() {
        return new FlowShopSolutionFileIO();
    }

    @Override
    protected String getDatasetName() {
        return "Ta100";
    }

}
