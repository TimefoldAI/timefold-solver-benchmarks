package ai.timefold.solver.benchmarks.micro.coldstart.problems;

import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.solver.SolverFactory;

import org.openjdk.jmh.infra.Blackhole;

public class TimeToFirstScoreProblem<Solution_> implements Problem {

    private final Class<? extends ConstraintProvider> constraintProviderClass;
    private final Class<Solution_> solutionClass;
    private final Class<?>[] entityClasses;
    private final Solution_ startingSolution;

    private SolverFactory<Solution_> solverFactory;

    public TimeToFirstScoreProblem(Class<? extends ConstraintProvider> constraintProviderClass, Solution_ solution,
            Class<?>... entityClasses) {
        this.constraintProviderClass = constraintProviderClass;
        this.solutionClass = (Class<Solution_>) solution.getClass();
        this.entityClasses = entityClasses;
        this.startingSolution = solution;
    }

    @Override
    public void setupTrial() {
        solverFactory = Example.createSolverFactory(constraintProviderClass, solutionClass, entityClasses);
    }

    @Override
    public void setupIteration() {
    }

    @Override
    public void setupInvocation() {
    }

    @Override
    public Object runInvocation(Blackhole blackhole) {
        blackhole.consume(startingSolution);
        return solverFactory.buildSolver().solve(startingSolution);
    }

    @Override
    public void tearDownInvocation() {
    }

    @Override
    public void tearDownIteration() {
    }

    @Override
    public void teardownTrial() {
        solverFactory = null;
    }
}
