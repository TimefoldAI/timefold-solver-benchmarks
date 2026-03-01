package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import java.util.Objects;

import ai.timefold.solver.benchmarks.examples.pas.domain.BedDesignation;
import ai.timefold.solver.benchmarks.examples.pas.domain.PatientAdmissionSchedule;
import ai.timefold.solver.benchmarks.examples.pas.persistence.PatientAdmissionScheduleSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.pas.score.PatientAdmissionScheduleConstraintProvider;
import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.api.domain.solution.SolutionFileIO;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;

public final class PatientAdmissionSchedulingProblem
        extends AbstractProblem<PatientAdmissionSchedule> {

    public PatientAdmissionSchedulingProblem(ScoreDirectorType scoreDirectorType) {
        super(Example.PATIENT_ADMISSION_SCHEDULING, scoreDirectorType);
    }

    @Override
    protected ScoreDirectorFactoryConfig buildScoreDirectorFactoryConfig(ScoreDirectorType scoreDirectorType) {
        var scoreDirectorFactoryConfig = buildInitialScoreDirectorFactoryConfig();
        var nonNullScoreDirectorType = Objects.requireNonNull(scoreDirectorType);
        if (nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS
                || nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS_JUSTIFIED) {
            return scoreDirectorFactoryConfig.withConstraintProviderClass(PatientAdmissionScheduleConstraintProvider.class);
        }
        throw new UnsupportedOperationException("Score director: " + scoreDirectorType);
    }

    @Override
    protected SolutionDescriptor<PatientAdmissionSchedule> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(PatientAdmissionSchedule.class, BedDesignation.class);
    }

    @Override
    protected SolutionFileIO<PatientAdmissionSchedule> createSolutionFileIO() {
        return new PatientAdmissionScheduleSolutionFileIO();
    }

    @Override
    protected String getDatasetName() {
        return "12";
    }

}
