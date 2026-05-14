package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import java.util.Objects;

import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceSolution;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.Talk;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.persistence.ConferenceSchedulingSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.score.ConferenceSchedulingConstraintProvider;
import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.api.domain.solution.SolutionFileIO;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;

public final class ConferenceSchedulingProblem extends AbstractProblem<ConferenceSolution> {

    public ConferenceSchedulingProblem(ScoreDirectorType scoreDirectorType) {
        super(Example.CONFERENCE_SCHEDULING, scoreDirectorType);
    }

    private ScoreDirectorFactoryConfig buildScoreDirectorFactoryConfig(ScoreDirectorType scoreDirectorType) {
        var scoreDirectorFactoryConfig = buildInitialScoreDirectorFactoryConfig();
        var nonNullScoreDirectorType = Objects.requireNonNull(scoreDirectorType);
        if (nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS
                || nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS_JUSTIFIED) {
            return scoreDirectorFactoryConfig.withConstraintProviderClass(ConferenceSchedulingConstraintProvider.class);
        }
        throw new UnsupportedOperationException("Score director: " + scoreDirectorType);
    }

    @Override
    protected SolverConfig buildSolverConfig(ScoreDirectorType scoreDirectorType) {
        return new SolverConfig()
                .withSolutionClass(ConferenceSolution.class)
                .withEntityClasses(Talk.class)
                .withScoreDirectorFactory(buildScoreDirectorFactoryConfig(scoreDirectorType));
    }

    @Override
    protected SolutionFileIO<ConferenceSolution> createSolutionFileIO() {
        return new ConferenceSchedulingSolutionFileIO();
    }

    @Override
    protected String getDatasetName() {
        return "216-18-20";
    }

}
