package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import ai.timefold.solver.benchmarks.examples.machinereassignment.domain.MachineReassignment;
import ai.timefold.solver.benchmarks.examples.machinereassignment.domain.MrProcessAssignment;
import ai.timefold.solver.benchmarks.examples.machinereassignment.persistence.MachineReassignmentSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.machinereassignment.score.MachineReassignmentConstraintProvider;
import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.api.domain.solution.SolutionFileIO;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;

public final class MachineReassignmentProblem
        extends AbstractProblem<MachineReassignment> {

    public MachineReassignmentProblem(ScoreDirectorType scoreDirectorType) {
        super(Example.MACHINE_REASSIGNMENT, scoreDirectorType);
    }

    private ScoreDirectorFactoryConfig buildScoreDirectorFactoryConfig(ScoreDirectorType scoreDirectorType) {
        var scoreDirectorFactoryConfig = buildInitialScoreDirectorFactoryConfig();
        return switch (scoreDirectorType) {
            case CONSTRAINT_STREAMS, CONSTRAINT_STREAMS_JUSTIFIED ->
                scoreDirectorFactoryConfig.withConstraintProviderClass(MachineReassignmentConstraintProvider.class);
        };
    }

    @Override
    protected SolverConfig buildSolverConfig(ScoreDirectorType scoreDirectorType) {
        return new SolverConfig()
                .withSolutionClass(MachineReassignment.class)
                .withEntityClasses(MrProcessAssignment.class)
                .withScoreDirectorFactory(buildScoreDirectorFactoryConfig(scoreDirectorType));
    }

    @Override
    protected SolutionFileIO<MachineReassignment> createSolutionFileIO() {
        return new MachineReassignmentSolutionFileIO();
    }

    @Override
    protected String getDatasetName() {
        return "a23";
    }

}
