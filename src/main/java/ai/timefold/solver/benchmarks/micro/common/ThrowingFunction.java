package ai.timefold.solver.benchmarks.micro.common;

import java.io.IOException;

public interface ThrowingFunction<A, B> {

    B apply(A a) throws IOException;

}
