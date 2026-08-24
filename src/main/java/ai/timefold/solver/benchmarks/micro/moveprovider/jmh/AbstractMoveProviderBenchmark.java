package ai.timefold.solver.benchmarks.micro.moveprovider.jmh;

import ai.timefold.solver.benchmarks.micro.moveprovider.problems.MoveProviderProblem;
import ai.timefold.solver.core.preview.api.move.Move;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Two scenarios per move provider, per {@code /home/agent/.claude/plans/zany-drifting-ocean.md}:
 * {@link #fewStepsManyMoves(Blackhole)} generates 10 moves per step over 2 steps (move generation
 * outweighs the neighborhood dataset-network update), {@link #manyStepsFewMoves(Blackhole)}
 * generates 2 moves per step over 10 steps (the network update outweighs move generation). Every
 * generated move is fed to the {@link Blackhole}, run or not, so none is ever thrown away.
 *
 * <p>
 * Also the single home of every parameter shared between the two benchmark subclasses (
 * {@code BasicVariableMoveProviderBenchmark}, {@code ListVariableMoveProviderBenchmark}) and
 * {@code MoveProviderBenchmarkTest}, so they cannot diverge.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public abstract class AbstractMoveProviderBenchmark {

    public static final int MAX_DRAW_ATTEMPTS_PER_MOVE = 100;
    public static final int FEW_STEP_COUNT = 2;
    public static final int MANY_MOVES_PER_STEP = 10;
    public static final int MANY_STEP_COUNT = 10;
    public static final int FEW_MOVES_PER_STEP = 2;
    public static final int UNASSIGN_EVERY_NTH = 5;
    public static final int MIN_SAMPLE_SIZE = 2;
    public static final int MAX_SAMPLE_SIZE = 10;
    public static final int TASK_ASSIGNING_MAX_SUB_LIST_SIZE = 15;
    // Memory bound only, not a measurement; see MoveProviderProblem's "Why no calculateScore()".
    public static final int FLUSH_EVERY_N_STEPS = 100;

    public MoveProviderProblem<?> problem;

    abstract protected MoveProviderProblem<?> createProblem();

    @Setup(Level.Trial)
    public void setupTrial() {
        problem = createProblem();
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
    public Move<?> fewStepsManyMoves(Blackhole blackhole) {
        return problem.runInvocation(FEW_STEP_COUNT, MANY_MOVES_PER_STEP, MAX_DRAW_ATTEMPTS_PER_MOVE, blackhole);
    }

    @Benchmark
    public Move<?> manyStepsFewMoves(Blackhole blackhole) {
        return problem.runInvocation(MANY_STEP_COUNT, FEW_MOVES_PER_STEP, MAX_DRAW_ATTEMPTS_PER_MOVE, blackhole);
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
