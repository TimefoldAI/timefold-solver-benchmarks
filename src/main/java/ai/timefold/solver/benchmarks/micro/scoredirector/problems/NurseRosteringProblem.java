package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import java.util.Objects;

import ai.timefold.solver.benchmarks.examples.nurserostering.domain.NurseRoster;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.ShiftAssignment;
import ai.timefold.solver.benchmarks.examples.nurserostering.persistence.NurseRosterSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.nurserostering.score.NurseRosteringConstraintProvider;
import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.api.domain.solution.SolutionFileIO;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;

public final class NurseRosteringProblem extends AbstractProblem<NurseRoster> {

    public NurseRosteringProblem(ScoreDirectorType scoreDirectorType) {
        super(Example.NURSE_ROSTERING, scoreDirectorType);
    }

    private ScoreDirectorFactoryConfig buildScoreDirectorFactoryConfig(ScoreDirectorType scoreDirectorType) {
        var scoreDirectorFactoryConfig = buildInitialScoreDirectorFactoryConfig();
        var nonNullScoreDirectorType = Objects.requireNonNull(scoreDirectorType);
        if (nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS
                || nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS_JUSTIFIED) {
            return scoreDirectorFactoryConfig.withConstraintProviderClass(NurseRosteringConstraintProvider.class);
        }
        throw new UnsupportedOperationException("Score director: " + scoreDirectorType);
    }

    @Override
    protected SolverConfig buildSolverConfig(ScoreDirectorType scoreDirectorType) {
        return new SolverConfig()
                .withSolutionClass(NurseRoster.class)
                .withEntityClasses(ShiftAssignment.class)
                .withScoreDirectorFactory(buildScoreDirectorFactoryConfig(scoreDirectorType));
    }

    @Override
    protected SolutionFileIO<NurseRoster> createSolutionFileIO() {
        return new NurseRosterSolutionFileIO();
    }

    @Override
    protected String getDatasetName() {
        return "medium_late01";
    }

}
