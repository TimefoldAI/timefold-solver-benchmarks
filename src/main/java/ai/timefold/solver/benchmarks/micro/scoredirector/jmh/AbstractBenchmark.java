package ai.timefold.solver.benchmarks.micro.scoredirector.jmh;

import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.benchmarks.micro.scoredirector.problems.Problem;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public abstract class AbstractBenchmark {

    /**
     * Batches this many independent draw-execute-score cycles into one JMH invocation,
     * by calling {@link Problem}'s per-invocation lifecycle in a plain loop
     * instead of letting JMH's {@code Level.Invocation} machinery drive it once per call.
     * Without this, JMH's own per-invocation bookkeeping lands on a single cheap operation instead of being amortized.
     */
    private static final int BATCH_SIZE = 100;

    public Problem problem;

    abstract protected ScoreDirectorType getScoreDirectorType();

    abstract protected Example getExample();

    @Setup(Level.Trial)
    public void setupTrial() {
        problem = getExample().create(getScoreDirectorType());
        problem.setupTrial();
    }

    @Setup(Level.Iteration)
    public void setupIteration() {
        problem.setupIteration();
    }

    @Benchmark
    @OperationsPerInvocation(BATCH_SIZE)
    public Object run(Blackhole blackhole) {
        Object result = null;
        for (var i = 0; i < BATCH_SIZE; i++) {
            problem.setupInvocation();
            result = problem.runInvocation();
            blackhole.consume(result);
            problem.tearDownInvocation();
        }
        return result;
    }

    @TearDown(Level.Iteration)
    public void teardownIteration() {
        problem.tearDownIteration();
    }

    @TearDown(Level.Trial)
    public void teardownTrial() {
        problem.teardownTrial();
    }

}
