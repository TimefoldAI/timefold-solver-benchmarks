package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import java.util.EnumSet;
import java.util.Objects;

import ai.timefold.solver.benchmarks.examples.examination.domain.Exam;
import ai.timefold.solver.benchmarks.examples.examination.domain.Examination;
import ai.timefold.solver.benchmarks.examples.examination.domain.FollowingExam;
import ai.timefold.solver.benchmarks.examples.examination.domain.LeadingExam;
import ai.timefold.solver.benchmarks.examples.examination.persistence.ExaminationSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.examination.score.ExaminationConstraintProvider;
import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.PreviewFeature;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

public final class ExaminationProblem extends AbstractProblem<Examination> {

    public ExaminationProblem(ScoreDirectorType scoreDirectorType) {
        super(Example.EXAMINATION, scoreDirectorType);
    }

    @Override
    protected ScoreDirectorFactoryConfig buildScoreDirectorFactoryConfig(ScoreDirectorType scoreDirectorType) {
        var scoreDirectorFactoryConfig = buildInitialScoreDirectorFactoryConfig();
        var nonNullScoreDirectorType = Objects.requireNonNull(scoreDirectorType);
        if (nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS
                || nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS_JUSTIFIED) {
            return scoreDirectorFactoryConfig.withConstraintProviderClass(ExaminationConstraintProvider.class);
        }
        throw new UnsupportedOperationException("Score director: " + scoreDirectorType);
    }

    @Override
    protected SolutionDescriptor<Examination> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(EnumSet.of(PreviewFeature.DECLARATIVE_SHADOW_VARIABLES),
                Examination.class, Exam.class, LeadingExam.class, FollowingExam.class);
    }

    @Override
    protected SolutionFileIO<Examination> createSolutionFileIO() {
        return new ExaminationSolutionFileIO();
    }

    @Override
    protected String getDatasetName() {
        return "comp_set8";
    }

}
