package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import ai.timefold.solver.benchmarks.examples.cloudbalancing.domain.CloudBalance;
import ai.timefold.solver.benchmarks.examples.cloudbalancing.domain.CloudProcess;
import ai.timefold.solver.benchmarks.examples.cloudbalancing.persistence.CloudBalanceSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.cloudbalancing.score.CloudBalancingConstraintProvider;
import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.api.domain.solution.SolutionFileIO;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;

public final class CloudBalancingProblem extends AbstractProblem<CloudBalance> {

    public CloudBalancingProblem(ScoreDirectorType scoreDirectorType) {
        super(Example.CLOUD_BALANCING, scoreDirectorType);
    }

    private ScoreDirectorFactoryConfig buildScoreDirectorFactoryConfig(ScoreDirectorType scoreDirectorType) {
        var scoreDirectorFactoryConfig = buildInitialScoreDirectorFactoryConfig();
        return switch (scoreDirectorType) {
            case CONSTRAINT_STREAMS, CONSTRAINT_STREAMS_JUSTIFIED ->
                scoreDirectorFactoryConfig.withConstraintProviderClass(CloudBalancingConstraintProvider.class);
        };
    }

    @Override
    protected SolverConfig buildSolverConfig(ScoreDirectorType scoreDirectorType) {
        return new SolverConfig()
                .withSolutionClass(CloudBalance.class)
                .withEntityClasses(CloudProcess.class)
                .withScoreDirectorFactory(buildScoreDirectorFactoryConfig(scoreDirectorType));
    }

    @Override
    protected SolutionFileIO<CloudBalance> createSolutionFileIO() {
        return new CloudBalanceSolutionFileIO();
    }

    @Override
    protected String getDatasetName() {
        return "1600-4800";
    }

}
