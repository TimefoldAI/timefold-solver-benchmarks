package ai.timefold.solver.benchmarks.micro.cloning.jmh;

import ai.timefold.solver.benchmarks.micro.cloning.problems.Example;
import ai.timefold.solver.benchmarks.micro.cloning.problems.Problem;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput) // We want to see cold starts.
public abstract class AbstractBenchmark {

    public Problem problem;

    abstract protected Example getExample();

    @Setup(Level.Trial)
    public void setupTrial() {
        problem = getExample().createProblem();
        problem.setupTrial();
    }

    @Setup(Level.Iteration)
    public void setupIteration() {
        problem.setupIteration();
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        problem.setupInvocation();
    }

    @Benchmark
    public Object run(Blackhole blackhole) {
        return problem.runInvocation(blackhole);
    }

    @TearDown(Level.Invocation)
    public void teardownInvocation() {
        problem.tearDownInvocation();
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
