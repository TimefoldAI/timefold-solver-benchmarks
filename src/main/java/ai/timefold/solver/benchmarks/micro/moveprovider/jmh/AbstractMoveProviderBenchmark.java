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
 * Two scenarios per move provider, per {@code /home/agent/.claude/plans/swift-pondering-fountain.md}:
 * {@link #singleDraw(Blackhole)} draws exactly one candidate move and commits it; {@link
 * #manyDraws(Blackhole)} draws {@link #MANY_DRAWS} candidates and commits only the last (move
 * generation cost dominates the commit/settle cost). Every generated move is fed to the {@link
 * Blackhole}, run or not, so none is ever thrown away.
 *
 * <p>
 * Every invocation is exactly one step, and its committed move is undone before the invocation
 * returns (see {@code MoveProviderProblem.runInvocation}): the working solution returns to exactly
 * its pre-invocation state every time, so nothing ever drifts within an iteration. Only the random
 * source used to draw candidates keeps advancing across invocations, so consecutive invocations
 * still draw different candidates, reproducibly, from the same per-iteration seed. The undo goes
 * through {@code MoveDirector.executeTemporaryWithoutScoring}, so no measured invocation ever
 * calculates score; the only exception is {@code MoveProviderProblem}'s bounded, off-the-clock
 * flush every {@link #FLUSH_EVERY_N_STEPS} invocations, unrelated to what is being measured here.
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
    public static final int SINGLE_DRAW = 1;
    public static final int MANY_DRAWS = 10;
    public static final int UNASSIGN_EVERY_NTH = 5;
    public static final int MIN_SAMPLE_SIZE = 2;
    public static final int MAX_SAMPLE_SIZE = 10;
    public static final int TASK_ASSIGNING_MAX_SUB_LIST_SIZE = 15;
    // Memory bound only, not a measurement; see MoveProviderProblem's maybeFlushConstraintStreamSession().
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
    public Move<?> singleDraw(Blackhole blackhole, MovedValueCounter counter) {
        return problem.runInvocation(SINGLE_DRAW, MAX_DRAW_ATTEMPTS_PER_MOVE, blackhole, counter);
    }

    @Benchmark
    public Move<?> manyDraws(Blackhole blackhole, MovedValueCounter counter) {
        return problem.runInvocation(MANY_DRAWS, MAX_DRAW_ATTEMPTS_PER_MOVE, blackhole, counter);
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
