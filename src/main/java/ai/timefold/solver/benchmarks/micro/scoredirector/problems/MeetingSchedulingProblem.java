package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import java.util.Objects;

import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.MeetingAssignment;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.MeetingSchedule;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.persistence.MeetingSchedulingSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.score.MeetingSchedulingConstraintProvider;
import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.api.domain.solution.SolutionFileIO;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;

public final class MeetingSchedulingProblem extends AbstractProblem<MeetingSchedule> {

    public MeetingSchedulingProblem(ScoreDirectorType scoreDirectorType) {
        super(Example.MEETING_SCHEDULING, scoreDirectorType);
    }

    private ScoreDirectorFactoryConfig buildScoreDirectorFactoryConfig(ScoreDirectorType scoreDirectorType) {
        var scoreDirectorFactoryConfig = buildInitialScoreDirectorFactoryConfig();
        var nonNullScoreDirectorType = Objects.requireNonNull(scoreDirectorType);
        if (nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS
                || nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS_JUSTIFIED) {
            return scoreDirectorFactoryConfig.withConstraintProviderClass(MeetingSchedulingConstraintProvider.class);
        }
        throw new UnsupportedOperationException("Score director: " + scoreDirectorType);
    }

    @Override
    protected SolverConfig buildSolverConfig(ScoreDirectorType scoreDirectorType) {
        return new SolverConfig()
                .withSolutionClass(MeetingSchedule.class)
                .withEntityClasses(MeetingAssignment.class)
                .withScoreDirectorFactory(buildScoreDirectorFactoryConfig(scoreDirectorType));
    }

    @Override
    protected SolutionFileIO<MeetingSchedule> createSolutionFileIO() {
        return new MeetingSchedulingSolutionFileIO();
    }

    @Override
    protected String getDatasetName() {
        return "100-320-5";
    }

}
