package ai.timefold.solver.benchmarks.micro.moveprovider.jmh;

import ai.timefold.solver.benchmarks.micro.moveprovider.problems.MoveProviderProblem;
import ai.timefold.solver.core.preview.api.move.Move;

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

/**
 * Two scenarios per move provider, one for each of the two costs a move provider carries.
 * {@link #commitMove(Blackhole, MovedValueCounter)} draws one candidate move, commits it, settles the
 * dataset network and undoes it, so its speed is the cost to <em>apply</em> a move.
 * {@link #drawOnly(Blackhole, MovedValueCounter)} draws {@link #DRAW_ONLY_DRAWS} candidates and
 * commits none, so its speed is the cost to <em>make</em> one. Every generated move is fed to the
 * {@link Blackhole}, committed or not, so none is ever thrown away.
 *
 * <p>
 * There used to be a third scenario, {@code manyDraws}, which drew ten candidates and committed the
 * last, meaning to let generation dominate the commit. It never did: the commit turned out to be
 * 83 to 98 % of both mixed scenarios. It was also exactly {@code commitMove + 9 × drawOnly}, so it
 * measured neither cost cleanly and added nothing once both are measured on their own.
 *
 * <p>
 * {@link #commitMove(Blackhole, MovedValueCounter)} undoes its committed move before the invocation
 * returns (see {@code MoveProviderProblem.runCommitMove}): the working solution returns to exactly
 * its pre-invocation state every time, so nothing ever drifts within an iteration. Only the random
 * source used to draw candidates keeps advancing across invocations, so consecutive invocations
 * still draw different candidates, reproducibly, from the same per-iteration seed. The undo goes
 * through {@code MoveDirector.executeTemporaryWithoutScoring}, so no measured invocation ever
 * calculates score - which also means neither scenario is a whole local-search step, because a real
 * step scores every candidate it draws. The only score calculation is {@code MoveProviderProblem}'s
 * bounded, off-the-clock flush every {@link #FLUSH_EVERY_N_STEPS} invocations, unrelated to what is
 * being measured here.
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
    /**
     * Frozen. A bi (joined) move iterator retires a left tuple that keeps probing empty, for the life
     * of one iterator, so a longer invocation draws from a more pruned pool and reports a faster
     * number. Changing this therefore changes the measured value, not only its precision, and it
     * invalidates every stored baseline. JMH does not record the operations for each invocation in
     * its JSON, so nothing detects such a change; the only guard is this note.
     */
    public static final int DRAW_ONLY_DRAWS = 500;
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

    @Benchmark
    public Move<?> commitMove(Blackhole blackhole, MovedValueCounter counter) {
        return problem.runCommitMove(MAX_DRAW_ATTEMPTS_PER_MOVE, blackhole, counter);
    }

    /**
     * {@link OperationsPerInvocation} makes the reported unit one drawn move rather than one
     * invocation of {@link #DRAW_ONLY_DRAWS} of them. Without it the speed would be an arbitrary
     * number that moves whenever the draw count is retuned, and the {@code movedValues} counter
     * divided by it would be the values for each invocation rather than the mean move size. JMH does
     * not scale an {@code AuxCounters} counter by this factor, verified against JMH 1.37, so the
     * quotient of the two is the mean move size directly.
     */
    @Benchmark
    @OperationsPerInvocation(DRAW_ONLY_DRAWS)
    public Move<?> drawOnly(Blackhole blackhole, MovedValueCounter counter) {
        return problem.runDrawOnly(DRAW_ONLY_DRAWS, MAX_DRAW_ATTEMPTS_PER_MOVE, blackhole, counter);
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
