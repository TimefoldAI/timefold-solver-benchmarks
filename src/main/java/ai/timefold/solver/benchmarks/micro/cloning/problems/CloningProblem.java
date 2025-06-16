package ai.timefold.solver.benchmarks.micro.cloning.problems;

import ai.timefold.solver.core.api.domain.solution.cloner.SolutionCloner;
import ai.timefold.solver.core.impl.domain.solution.cloner.FieldAccessingSolutionCloner;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;

import org.openjdk.jmh.infra.Blackhole;

public class CloningProblem<Solution_> implements Problem {

    private final Solution_ startingSolution;
    private final SolutionDescriptor<Solution_> solutionDescriptor;

    private SolutionCloner<Solution_> cloner;

    public CloningProblem(Solution_ startingSolution, SolutionDescriptor<Solution_> solutionDescriptor) {
        this.startingSolution = startingSolution;
        this.solutionDescriptor = solutionDescriptor;
    }

    @Override
    public void setupTrial() {
    }

    @Override
    public void setupIteration() {
        cloner = new FieldAccessingSolutionCloner<>(solutionDescriptor);
    }

    @Override
    public void setupInvocation() {
    }

    @Override
    public Object runInvocation(Blackhole blackhole) {
        blackhole.consume(startingSolution);
        return cloner.cloneSolution(startingSolution);
    }

    @Override
    public void tearDownInvocation() {
    }

    @Override
    public void tearDownIteration() {
        cloner = null;
    }

    @Override
    public void teardownTrial() {
    }

}
