package ai.timefold.solver.benchmarks.micro.moveprovider.jmh;

import ai.timefold.solver.benchmarks.micro.moveprovider.ListMoveProviderCase;
import ai.timefold.solver.benchmarks.micro.moveprovider.problems.MoveProviderProblem;

import org.openjdk.jmh.annotations.Param;

public class ListVariableMoveProviderBenchmark extends AbstractMoveProviderBenchmark {

    @Param
    public ListMoveProviderCase listMoveProvider;

    @Override
    protected MoveProviderProblem<?> createProblem() {
        return new MoveProviderProblem<>(listMoveProvider);
    }

}
