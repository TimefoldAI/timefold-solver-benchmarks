package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Customer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Vehicle;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.VehicleRoutingSolution;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.persistence.VehicleRoutingSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.score.VehicleRoutingConstraintProvider;
import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.api.domain.solution.SolutionFileIO;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;

public final class VehicleRoutingProblem extends AbstractProblem<VehicleRoutingSolution> {

    public VehicleRoutingProblem(ScoreDirectorType scoreDirectorType) {
        super(Example.VEHICLE_ROUTING, scoreDirectorType);
    }

    private ScoreDirectorFactoryConfig buildScoreDirectorFactoryConfig(ScoreDirectorType scoreDirectorType) {
        var scoreDirectorFactoryConfig = buildInitialScoreDirectorFactoryConfig();
        return switch (scoreDirectorType) {
            case CONSTRAINT_STREAMS, CONSTRAINT_STREAMS_JUSTIFIED ->
                scoreDirectorFactoryConfig.withConstraintProviderClass(VehicleRoutingConstraintProvider.class);
        };
    }

    @Override
    protected SolverConfig buildSolverConfig(ScoreDirectorType scoreDirectorType) {
        return new SolverConfig()
                .withSolutionClass(VehicleRoutingSolution.class)
                .withEntityClasses(Vehicle.class, Customer.class)
                .withScoreDirectorFactory(buildScoreDirectorFactoryConfig(scoreDirectorType));
    }

    @Override
    protected SolutionFileIO<VehicleRoutingSolution> createSolutionFileIO() {
        return new VehicleRoutingSolutionFileIO();
    }

    @Override
    protected String getDatasetName() {
        return "RC2_4_10";
    }

}
