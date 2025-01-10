package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import java.io.File;
import java.util.Objects;

import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceSolution;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.Talk;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.persistence.ConferenceSchedulingSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.score.ConferenceSchedulingConstraintProvider;
import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.api.score.stream.ConstraintStreamImplType;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;

public final class ConferenceSchedulingProblem extends AbstractProblem<ConferenceSolution> {

    public ConferenceSchedulingProblem(ScoreDirectorType scoreDirectorType) {
        super(Example.CONFERENCE_SCHEDULING, scoreDirectorType);
    }

    @Override
    protected ScoreDirectorFactoryConfig buildScoreDirectorFactoryConfig(ScoreDirectorType scoreDirectorType) {
        var scoreDirectorFactoryConfig = buildInitialScoreDirectorFactoryConfig();
        var nonNullScoreDirectorType = Objects.requireNonNull(scoreDirectorType);
        if (nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS
                || nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS_JUSTIFIED) {
            return scoreDirectorFactoryConfig
                    .withConstraintProviderClass(ConferenceSchedulingConstraintProvider.class)
                    .withConstraintStreamImplType(ConstraintStreamImplType.BAVET);
        }
        throw new UnsupportedOperationException("Score director: " + scoreDirectorType);
    }

    @Override
    protected SolutionDescriptor<ConferenceSolution> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(ConferenceSolution.class, Talk.class);
    }

    @Override
    protected ConferenceSolution readOriginalSolution() {
        return new ConferenceSchedulingSolutionFileIO()
                .read(new File("data/conferencescheduling/conferencescheduling-216-18-20.json"));
    }

}
