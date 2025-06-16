package ai.timefold.solver.benchmarks.micro.cloning.problems;

import java.io.File;

import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Customer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Vehicle;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.VehicleRoutingSolution;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.timewindowed.TimeWindowedCustomer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.persistence.VehicleRoutingSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.score.VehicleRoutingConstraintProvider;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicType;
import ai.timefold.solver.core.config.solver.PreviewFeature;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.impl.solver.DefaultSolverFactory;

public enum Example {

    VEHICLE_ROUTING(VehicleRoutingConstraintProvider.class, VehicleRoutingSolution.class, Vehicle.class, Customer.class,
            TimeWindowedCustomer.class) {
        @Override
        <Solution_> Solution_ loadDataset() {
            return (Solution_) new VehicleRoutingSolutionFileIO()
                    .read(new File("data/vehiclerouting/vehiclerouting-Flanders2-30000-300.json"));
        }
    };

    private final Class<? extends ConstraintProvider> constraintProviderClass;
    private final Class<?> solutionClass;
    private final Class<?>[] entityClasses;

    Example(Class<? extends ConstraintProvider> constraintProviderClass, Class<?> solutionClass, Class<?>... entityClasses) {
        this.constraintProviderClass = constraintProviderClass;
        this.solutionClass = solutionClass;
        this.entityClasses = entityClasses;
    }

    abstract <Solution_> Solution_ loadDataset();

    @SuppressWarnings("unchecked")
    public <Solution_> Problem createProblem() {
        var solverFactory =
                (DefaultSolverFactory<Solution_>) createSolverFactory(constraintProviderClass, solutionClass, entityClasses);
        var initializedSolution = (Solution_) loadDataset();
        return new CloningProblem<>(initializedSolution, solverFactory.getSolutionDescriptor());
    }

    public static <Solution_> SolverFactory<Solution_> createSolverFactory(
            Class<? extends ConstraintProvider> constraintProviderClass, Class<?> solutionClass, Class<?>... entityClasses) {
        var constructionHeuristicConfig = new ConstructionHeuristicPhaseConfig();
        if (solutionClass == TspSolution.class) { // Otherwise all hell breaks loose.
            constructionHeuristicConfig.withConstructionHeuristicType(ConstructionHeuristicType.FIRST_FIT);
        }
        return SolverFactory.create(new SolverConfig()
                .withPreviewFeature(PreviewFeature.DECLARATIVE_SHADOW_VARIABLES)
                .withSolutionClass(solutionClass)
                .withEntityClasses(entityClasses)
                .withConstraintProviderClass(constraintProviderClass)
                .withPhases(constructionHeuristicConfig));
    }

}
