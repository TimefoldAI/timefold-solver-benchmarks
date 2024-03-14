package ai.timefold.solver.benchmarks.jmh.common;

import java.io.IOException;

public interface ThrowingFunction<A, B> {

    B apply(A a) throws IOException;

}
