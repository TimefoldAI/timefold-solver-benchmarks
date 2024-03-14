package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import java.io.File;
import java.util.Objects;

import ai.timefold.solver.benchmarks.examples.taskassigning.domain.Employee;
import ai.timefold.solver.benchmarks.examples.taskassigning.domain.Task;
import ai.timefold.solver.benchmarks.examples.taskassigning.domain.TaskAssigningSolution;
import ai.timefold.solver.benchmarks.examples.taskassigning.persistence.TaskAssigningSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.taskassigning.score.TaskAssigningConstraintProvider;
import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.api.score.stream.ConstraintStreamImplType;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

public final class TaskAssigningProblem extends AbstractProblem<TaskAssigningSolution> {

    public TaskAssigningProblem(ScoreDirectorType scoreDirectorType) {
        super(Example.TASK_ASSIGNING, scoreDirectorType);
    }

    @Override
    protected ScoreDirectorFactoryConfig buildScoreDirectorFactoryConfig(ScoreDirectorType scoreDirectorType) {
        var scoreDirectorFactoryConfig = buildInitialScoreDirectorFactoryConfig();
        var nonNullScoreDirectorType = Objects.requireNonNull(scoreDirectorType);
        if (nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS
                || nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS_JUSTIFIED) {
            return scoreDirectorFactoryConfig
                    .withConstraintProviderClass(TaskAssigningConstraintProvider.class)
                    .withConstraintStreamImplType(ConstraintStreamImplType.BAVET);
        }
        throw new UnsupportedOperationException("Score director: " + scoreDirectorType);
    }

    @Override
    protected SolutionDescriptor<TaskAssigningSolution> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(TaskAssigningSolution.class, Employee.class, Task.class);
    }

    @Override
    protected TaskAssigningSolution readOriginalSolution() {
        final SolutionFileIO<TaskAssigningSolution> solutionFileIO = new TaskAssigningSolutionFileIO();
        return solutionFileIO.read(new File("data/taskassigning/taskassigning-500-20.json"));
    }

}
