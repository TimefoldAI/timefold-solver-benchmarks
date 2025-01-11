package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import ai.timefold.solver.benchmarks.examples.cloudbalancing.domain.CloudBalance;
import ai.timefold.solver.benchmarks.examples.cloudbalancing.domain.CloudProcess;
import ai.timefold.solver.benchmarks.examples.cloudbalancing.optional.score.CloudBalancingIncrementalScoreCalculator;
import ai.timefold.solver.benchmarks.examples.cloudbalancing.optional.score.CloudBalancingMapBasedEasyScoreCalculator;
import ai.timefold.solver.benchmarks.examples.cloudbalancing.persistence.CloudBalanceSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.cloudbalancing.score.CloudBalancingConstraintProvider;
import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

public final class CloudBalancingProblem extends AbstractProblem<CloudBalance> {

    public CloudBalancingProblem(ScoreDirectorType scoreDirectorType) {
        super(Example.CLOUD_BALANCING, scoreDirectorType);
    }

    @Override
    protected ScoreDirectorFactoryConfig buildScoreDirectorFactoryConfig(ScoreDirectorType scoreDirectorType) {
        var scoreDirectorFactoryConfig = buildInitialScoreDirectorFactoryConfig();
        return switch (scoreDirectorType) {
            case CONSTRAINT_STREAMS, CONSTRAINT_STREAMS_JUSTIFIED ->
                scoreDirectorFactoryConfig.withConstraintProviderClass(CloudBalancingConstraintProvider.class);
            case EASY ->
                scoreDirectorFactoryConfig.withEasyScoreCalculatorClass(CloudBalancingMapBasedEasyScoreCalculator.class);
            case INCREMENTAL ->
                scoreDirectorFactoryConfig.withIncrementalScoreCalculatorClass(CloudBalancingIncrementalScoreCalculator.class);
        };
    }

    @Override
    protected SolutionDescriptor<CloudBalance> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(CloudBalance.class, CloudProcess.class);
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
