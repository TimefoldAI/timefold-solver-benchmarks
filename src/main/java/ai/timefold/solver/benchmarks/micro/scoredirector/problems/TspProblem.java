package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import ai.timefold.solver.benchmarks.examples.tsp.domain.Tour;
import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;
import ai.timefold.solver.benchmarks.examples.tsp.domain.Visit;
import ai.timefold.solver.benchmarks.examples.tsp.optional.score.TspEasyScoreCalculator;
import ai.timefold.solver.benchmarks.examples.tsp.optional.score.TspIncrementalScoreCalculator;
import ai.timefold.solver.benchmarks.examples.tsp.persistence.TspSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.tsp.score.TspConstraintProvider;
import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

public final class TspProblem extends AbstractProblem<TspSolution> {

    public TspProblem(ScoreDirectorType scoreDirectorType) {
        super(Example.TSP, scoreDirectorType);
    }

    @Override
    protected ScoreDirectorFactoryConfig buildScoreDirectorFactoryConfig(ScoreDirectorType scoreDirectorType) {
        var scoreDirectorFactoryConfig = buildInitialScoreDirectorFactoryConfig();
        return switch (scoreDirectorType) {
            case CONSTRAINT_STREAMS, CONSTRAINT_STREAMS_JUSTIFIED ->
                scoreDirectorFactoryConfig.withConstraintProviderClass(TspConstraintProvider.class);
            case EASY -> scoreDirectorFactoryConfig.withEasyScoreCalculatorClass(TspEasyScoreCalculator.class);
            case INCREMENTAL ->
                scoreDirectorFactoryConfig.withIncrementalScoreCalculatorClass(TspIncrementalScoreCalculator.class);
        };
    }

    @Override
    protected SolutionDescriptor<TspSolution> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(TspSolution.class, Tour.class, Visit.class);
    }

    @Override
    protected SolutionFileIO<TspSolution> createSolutionFileIO() {
        return new TspSolutionFileIO();
    }

    @Override
    protected String getDatasetName() {
        return "vm1084";
    }

}
