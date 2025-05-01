package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import java.util.EnumSet;

import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Customer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Vehicle;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.VehicleRoutingSolution;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.timewindowed.TimeWindowedCustomer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.optional.score.VehicleRoutingEasyScoreCalculator;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.optional.score.VehicleRoutingIncrementalScoreCalculator;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.persistence.VehicleRoutingSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.score.VehicleRoutingConstraintProvider;
import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.PreviewFeature;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

public final class VehicleRoutingProblem extends AbstractProblem<VehicleRoutingSolution> {

    public VehicleRoutingProblem(ScoreDirectorType scoreDirectorType) {
        super(Example.VEHICLE_ROUTING, scoreDirectorType);
    }

    @Override
    protected ScoreDirectorFactoryConfig buildScoreDirectorFactoryConfig(ScoreDirectorType scoreDirectorType) {
        var scoreDirectorFactoryConfig = buildInitialScoreDirectorFactoryConfig();
        return switch (scoreDirectorType) {
            case CONSTRAINT_STREAMS, CONSTRAINT_STREAMS_JUSTIFIED ->
                scoreDirectorFactoryConfig.withConstraintProviderClass(VehicleRoutingConstraintProvider.class);
            case EASY -> scoreDirectorFactoryConfig.withEasyScoreCalculatorClass(VehicleRoutingEasyScoreCalculator.class);
            case INCREMENTAL ->
                scoreDirectorFactoryConfig.withIncrementalScoreCalculatorClass(VehicleRoutingIncrementalScoreCalculator.class);
        };
    }

    @Override
    protected SolutionDescriptor<VehicleRoutingSolution> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(EnumSet.of(PreviewFeature.DECLARATIVE_SHADOW_VARIABLES),
                VehicleRoutingSolution.class, Vehicle.class, Customer.class, TimeWindowedCustomer.class);
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
