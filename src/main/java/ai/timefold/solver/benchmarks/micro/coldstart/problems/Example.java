package ai.timefold.solver.benchmarks.micro.coldstart.problems;

import java.io.File;

import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceSolution;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.Talk;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.persistence.ConferenceSchedulingXlsxFileIO;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.score.ConferenceSchedulingConstraintProvider;
import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;
import ai.timefold.solver.benchmarks.examples.tsp.domain.Visit;
import ai.timefold.solver.benchmarks.examples.tsp.persistence.TspSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.tsp.score.TspConstraintProvider;
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
import ai.timefold.solver.core.config.solver.SolverConfig;

public enum Example {

    CONFERENCE_SCHEDULING(ConferenceSchedulingConstraintProvider.class, ConferenceSolution.class, Talk.class) {
        @Override
        <Solution_> Solution_ loadDataset() {
            var io = new ConferenceSchedulingXlsxFileIO();
            return (Solution_) io.read(new File("data/conferencescheduling/conferencescheduling-216-18-20.xlsx"));
        }
    },
    TSP(TspConstraintProvider.class, TspSolution.class, Visit.class) {
        @Override
        <Solution_> Solution_ loadDataset() {
            while (true) {
                try {
                    var io = new TspSolutionFileIO();
                    return (Solution_) io.read(new File("data/tsp/tsp-lu980.json"));
                } catch (StackOverflowError stackOverflowError) {
                    // Do nothing.
                }
            }
        }
    },
    VEHICLE_ROUTING(VehicleRoutingConstraintProvider.class, VehicleRoutingSolution.class, Vehicle.class, Customer.class,
            TimeWindowedCustomer.class) {
        @Override
        <Solution_> Solution_ loadDataset() {
            var io = new VehicleRoutingSolutionFileIO();
            return (Solution_) io.read(new File("data/vehiclerouting/vehiclerouting-belgium-tw-n2750-k55.json"));
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

    public <Solution_> Problem createTimeToFirstScore() {
        var solverFactory = createSolverFactory(constraintProviderClass, solutionClass, entityClasses);
        var uninitializedSolution = (Solution_) loadDataset();
        var initializedSolution = solverFactory.buildSolver().solve(uninitializedSolution);
        return new TimeToFirstScoreProblem<>(constraintProviderClass, initializedSolution, entityClasses);
    }

    public static <Solution_> SolverFactory<Solution_> createSolverFactory(
            Class<? extends ConstraintProvider> constraintProviderClass, Class<?> solutionClass, Class<?>... entityClasses) {
        var constructionHeuristicConfig = new ConstructionHeuristicPhaseConfig();
        if (solutionClass == TspSolution.class) { // Otherwise all hell breaks loose.
            constructionHeuristicConfig.withConstructionHeuristicType(ConstructionHeuristicType.FIRST_FIT);
        }
        return SolverFactory.<Solution_> create(new SolverConfig()
                .withSolutionClass(solutionClass)
                .withEntityClasses(entityClasses)
                .withConstraintProviderClass(constraintProviderClass)
                .withPhases(constructionHeuristicConfig));
    }

    public Problem createTimeToSolverFactory() {
        return new TimeToSolverFactoryProblem(constraintProviderClass, solutionClass, entityClasses);
    }

}
