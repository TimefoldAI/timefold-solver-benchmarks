package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import java.util.Objects;

import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.CourseSchedule;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.Lecture;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.persistence.CurriculumCourseSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.score.CurriculumCourseConstraintProvider;
import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.api.domain.solution.SolutionFileIO;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;

public final class CurriculumCourseProblem extends AbstractProblem<CourseSchedule> {

    public CurriculumCourseProblem(ScoreDirectorType scoreDirectorType) {
        super(Example.CURRICULUM_COURSE, scoreDirectorType);
    }

    private ScoreDirectorFactoryConfig buildScoreDirectorFactoryConfig(ScoreDirectorType scoreDirectorType) {
        var scoreDirectorFactoryConfig = buildInitialScoreDirectorFactoryConfig();
        var nonNullScoreDirectorType = Objects.requireNonNull(scoreDirectorType);
        if (nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS
                || nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS_JUSTIFIED) {
            return scoreDirectorFactoryConfig.withConstraintProviderClass(CurriculumCourseConstraintProvider.class);
        }
        throw new UnsupportedOperationException("Score director: " + scoreDirectorType);
    }

    @Override
    protected SolverConfig buildSolverConfig(ScoreDirectorType scoreDirectorType) {
        return new SolverConfig()
                .withSolutionClass(CourseSchedule.class)
                .withEntityClasses(Lecture.class)
                .withScoreDirectorFactory(buildScoreDirectorFactoryConfig(scoreDirectorType));
    }

    @Override
    protected SolutionFileIO<CourseSchedule> createSolutionFileIO() {
        return new CurriculumCourseSolutionFileIO();
    }

    @Override
    protected String getDatasetName() {
        return "comp07";
    }

}
