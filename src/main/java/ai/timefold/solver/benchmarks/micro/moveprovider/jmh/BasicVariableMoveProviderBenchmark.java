package ai.timefold.solver.benchmarks.micro.moveprovider.jmh;

import ai.timefold.solver.benchmarks.micro.moveprovider.BasicMoveProviderCase;
import ai.timefold.solver.benchmarks.micro.moveprovider.problems.MoveProviderProblem;

import org.openjdk.jmh.annotations.Param;

public class BasicVariableMoveProviderBenchmark extends AbstractMoveProviderBenchmark {

    @Param
    public BasicMoveProviderCase basicMoveProvider;

    @Override
    protected MoveProviderProblem<?> createProblem() {
        return new MoveProviderProblem<>(basicMoveProvider);
    }

}
