package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import java.util.Objects;

import ai.timefold.solver.benchmarks.examples.nurserostering.domain.NurseRoster;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.ShiftAssignment;
import ai.timefold.solver.benchmarks.examples.nurserostering.persistence.NurseRosterSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.nurserostering.score.NurseRosteringConstraintProvider;
import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

public final class NurseRosteringProblem extends AbstractProblem<NurseRoster> {

    public NurseRosteringProblem(ScoreDirectorType scoreDirectorType) {
        super(Example.NURSE_ROSTERING, scoreDirectorType);
    }

    @Override
    protected ScoreDirectorFactoryConfig buildScoreDirectorFactoryConfig(ScoreDirectorType scoreDirectorType) {
        var scoreDirectorFactoryConfig = buildInitialScoreDirectorFactoryConfig();
        var nonNullScoreDirectorType = Objects.requireNonNull(scoreDirectorType);
        if (nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS
                || nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS_JUSTIFIED) {
            return scoreDirectorFactoryConfig.withConstraintProviderClass(NurseRosteringConstraintProvider.class);
        }
        throw new UnsupportedOperationException("Score director: " + scoreDirectorType);
    }

    @Override
    protected SolutionDescriptor<NurseRoster> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(NurseRoster.class, ShiftAssignment.class);
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
