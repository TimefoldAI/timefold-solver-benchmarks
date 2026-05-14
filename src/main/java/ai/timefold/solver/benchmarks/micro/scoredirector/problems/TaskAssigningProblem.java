package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import java.util.Objects;

import ai.timefold.solver.benchmarks.examples.taskassigning.domain.Employee;
import ai.timefold.solver.benchmarks.examples.taskassigning.domain.Task;
import ai.timefold.solver.benchmarks.examples.taskassigning.domain.TaskAssigningSolution;
import ai.timefold.solver.benchmarks.examples.taskassigning.persistence.TaskAssigningSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.taskassigning.score.TaskAssigningConstraintProvider;
import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.api.domain.solution.SolutionFileIO;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;

public final class TaskAssigningProblem extends AbstractProblem<TaskAssigningSolution> {

    public TaskAssigningProblem(ScoreDirectorType scoreDirectorType) {
        super(Example.TASK_ASSIGNING, scoreDirectorType);
    }

    private ScoreDirectorFactoryConfig buildScoreDirectorFactoryConfig(ScoreDirectorType scoreDirectorType) {
        var scoreDirectorFactoryConfig = buildInitialScoreDirectorFactoryConfig();
        var nonNullScoreDirectorType = Objects.requireNonNull(scoreDirectorType);
        if (nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS
                || nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS_JUSTIFIED) {
            return scoreDirectorFactoryConfig.withConstraintProviderClass(TaskAssigningConstraintProvider.class);
        }
        throw new UnsupportedOperationException("Score director: " + scoreDirectorType);
    }

    @Override
    protected SolverConfig buildSolverConfig(ScoreDirectorType scoreDirectorType) {
        return new SolverConfig()
                .withSolutionClass(TaskAssigningSolution.class)
                .withEntityClasses(Employee.class, Task.class)
                .withScoreDirectorFactory(buildScoreDirectorFactoryConfig(scoreDirectorType));
    }

    @Override
    protected SolutionFileIO<TaskAssigningSolution> createSolutionFileIO() {
        return new TaskAssigningSolutionFileIO();
    }

    @Override
    protected String getDatasetName() {
        return "500-20";
    }

}
