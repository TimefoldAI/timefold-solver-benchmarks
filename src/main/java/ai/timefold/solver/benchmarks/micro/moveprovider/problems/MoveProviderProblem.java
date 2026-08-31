package ai.timefold.solver.benchmarks.micro.moveprovider.problems;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.benchmarks.micro.moveprovider.Example;
import ai.timefold.solver.benchmarks.micro.moveprovider.MoveProviderCase;
import ai.timefold.solver.benchmarks.micro.moveprovider.jmh.AbstractMoveProviderBenchmark;
import ai.timefold.solver.benchmarks.micro.moveprovider.jmh.MovedValueCounter;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.core.impl.localsearch.scope.LocalSearchPhaseScope;
import ai.timefold.solver.core.impl.localsearch.scope.LocalSearchStepScope;
import ai.timefold.solver.core.impl.neighborhood.NeighborhoodsBasedMoveRepository;
import ai.timefold.solver.core.impl.neighborhood.stream.DefaultMoveStreamFactory;
import ai.timefold.solver.core.impl.score.constraint.ConstraintMatchPolicy;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;
import ai.timefold.solver.core.impl.score.director.ScoreDirectorFactory;
import ai.timefold.solver.core.impl.score.director.ScoreDirectorFactoryFactory;
import ai.timefold.solver.core.impl.solver.DefaultSolverFactory;
import ai.timefold.solver.core.impl.solver.random.RandomSource;
import ai.timefold.solver.core.impl.solver.scope.SolverScope;
import ai.timefold.solver.core.preview.api.move.Move;
import ai.timefold.solver.core.preview.api.neighborhood.MoveProvider;

import org.openjdk.jmh.infra.Blackhole;

/**
 * Runs one {@link MoveProviderCase} through the two scenarios the benchmark measures.
 * {@link #runCommitMove} draws one candidate move, commits it, settles the dataset network, then
 * undoes it before the invocation returns; its speed is the cost to apply a move.
 * {@link #runDrawOnly} draws many candidates and commits none; its speed is the cost to make one.
 *
 * <p>
 * Undoing every invocation, instead of letting committed moves accumulate across an iteration, is
 * what keeps a directional move provider (one whose move only ever changes an entity's variable in
 * one direction, e.g. an assign-only or unassign-only provider) from permanently draining whatever
 * finite pool of null/non-null entities its dataset started with - which used to crash 11 of the 25
 * move providers once a JMH iteration ran long enough. {@link #setupIteration()} still resets
 * everything once per iteration (a fresh clone of the original solution, a fresh
 * {@link RandomSource#seeded(long) seeded(0)}, and a fresh {@link NeighborhoodsBasedMoveRepository})
 * - not because undo needs it, but as a cross-iteration reproducibility guarantee and a safety net.
 * Only the random source used to draw candidates keeps running across invocations within one
 * iteration, so consecutive invocations still draw different candidates, reproducibly.
 *
 * <p>
 * Undo goes through {@code MoveDirector.executeTemporaryWithoutScoring} - the same "record while a
 * move is applied, then replay in reverse" mechanism local search itself uses for temporary moves
 * via {@code executeTemporary}, minus the {@code calculateScore()} every {@code executeTemporary}
 * overload otherwise forces (they all exist to learn a move's score effect; this benchmark has no
 * use for it and does not want to pay for it - see that method's javadoc). This benchmark therefore
 * still never calculates score, exactly as it never did before undo existed here at all.
 */
public final class MoveProviderProblem<Solution_> {

    private final MoveProviderCase moveProviderCase;
    private final Example example;
    private final SolutionDescriptor<Solution_> solutionDescriptor;
    private final ScoreDirectorFactory<Solution_, ?> scoreDirectorFactory;
    private final Solution_ originalSolution;

    private DefaultMoveStreamFactory<Solution_> moveStreamFactory;
    private MoveProvider<Solution_> moveProvider;
    private InnerScoreDirector<Solution_, ?> scoreDirector;
    private NeighborhoodsBasedMoveRepository<Solution_> moveRepository;
    private LocalSearchPhaseScope<Solution_> phaseScope;
    private long invocationCounter;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public MoveProviderProblem(MoveProviderCase moveProviderCase) {
        this.moveProviderCase = Objects.requireNonNull(moveProviderCase);
        this.example = moveProviderCase.getExample();
        var solverConfig = example.buildSolverConfig();
        this.solutionDescriptor = ((DefaultSolverFactory) SolverFactory.create(solverConfig)).getSolutionDescriptor();
        // A local, NO_ASSERT score director factory. Deliberately not ScoreDirectorType's
        // PHASE_ASSERT-hardcoded helper from the score-director benchmark: this benchmark should
        // not measure assertions.
        this.scoreDirectorFactory =
                buildScoreDirectorFactory(solverConfig.getScoreDirectorFactoryConfig(), solutionDescriptor);
        this.originalSolution = example.loadDataset();
    }

    private static <Solution_, Score_ extends Score<Score_>> ScoreDirectorFactory<Solution_, Score_>
            buildScoreDirectorFactory(ScoreDirectorFactoryConfig scoreDirectorFactoryConfig,
                    SolutionDescriptor<Solution_> solutionDescriptor) {
        var scoreDirectorFactoryFactory = new ScoreDirectorFactoryFactory<Solution_, Score_>(scoreDirectorFactoryConfig);
        return scoreDirectorFactoryFactory.buildScoreDirectorFactory(EnvironmentMode.NO_ASSERT, solutionDescriptor);
    }

    public void setupTrial() {
        scoreDirector = scoreDirectorFactory.createScoreDirectorBuilder()
                .withConstraintMatchPolicy(ConstraintMatchPolicy.DISABLED)
                .build();
    }

    @SuppressWarnings("unchecked")
    public void setupIteration() {
        // We only care about incremental performance; therefore calculate the entire solution outside of invocation.
        scoreDirector.setWorkingSolution(scoreDirector.cloneSolution(originalSolution));
        scoreDirector.updateShadowVariables();
        scoreDirector.calculateScore();
        // A fresh factory and provider every iteration, same as every production call site
        // (DefaultLocalSearchPhaseFactory, DefaultNeighborhoodTester) pairs one factory with one
        // repository and never reuses the factory for a second build(). Reusing the factory across
        // iterations left every previous iteration's enumerating streams registered in
        // EnumeratingStreamFactory.sharingStreamMap forever (build() creates fresh predicate/joiner
        // lambda instances each call, so the equals()-based node-sharing cache never recognizes them
        // as duplicates) - each new session then had to build and populate all of them, causing
        // performance to degrade every iteration.
        moveStreamFactory = new DefaultMoveStreamFactory<>(solutionDescriptor, EnvironmentMode.NO_ASSERT);
        moveProvider = (MoveProvider<Solution_>) moveProviderCase.createMoveProvider(solutionDescriptor.getMetaModel());
        // Prepare the lifecycle. A fresh repository every iteration: its dataset session binds to
        // this iteration's working solution.
        moveRepository = new NeighborhoodsBasedMoveRepository<>(moveStreamFactory, List.of(moveProvider));
        var solverScope = new SolverScope<Solution_>();
        solverScope.setScoreDirector(scoreDirector);
        solverScope.setWorkingRandom(RandomSource.seeded(0)); // Fully reproducible random selection.
        moveRepository.solvingStarted(solverScope);
        phaseScope = new LocalSearchPhaseScope<>(solverScope, 0);
        moveRepository.phaseStarted(phaseScope); // Also calls scoreDirector.setMoveRepository(moveRepository).
        invocationCounter = 0;
    }

    /**
     * Draws one candidate, executes it, settles the dataset network, then undoes it before
     * returning - so the working solution is exactly as it was before this invocation, regardless of
     * which move got drawn. Undoing goes through
     * {@code MoveDirector.executeTemporaryWithoutScoring}, so no {@code calculateScore()} is ever
     * called - not by this method, not by anything it calls. No score is calculated at all, so this
     * is not a whole local-search step; it is the cost to apply one move and settle after it.
     * <p>
     * The settle happens between execute and undo, not after both: the undo dirties the dataset
     * network's tuples again on the way back, so settling only after the full round-trip would
     * settle a net-zero delta, and the network's own coalescing collapses that into a degenerate
     * update that skips exactly the terminal work (filter flips, list mutation, indexer
     * re-bucketing) this benchmark exists to measure.
     *
     * <p>
     * Exactly one draw, and the count is not a parameter. The CI report reads its
     * {@code Values/move} column from this scenario, which is only correct while the count is one;
     * and a second draw would only dilute what this scenario measures.
     *
     * @return the committed move - already undone by the time this returns - kept only so JMH
     *         cannot optimize the invocation away
     */
    public Move<Solution_> runCommitMove(int maxDrawAttemptsPerMove, Blackhole blackhole, MovedValueCounter counter) {
        var stepScope = new LocalSearchStepScope<>(phaseScope, (int) invocationCounter++);
        moveRepository.stepStarted(stepScope);
        var lastMove = drawMoves(moveRepository.iterator(), 1, maxDrawAttemptsPerMove, blackhole, counter);
        // The step must happen; drawMove() has already guaranteed a non-null move.
        scoreDirector.getMoveDirector().executeTemporaryWithoutScoring(lastMove, workingSolution -> {
            moveRepository.stepEnded(stepScope); // Settles the dataset network; this is what this scenario measures.
            return null;
        });
        return lastMove;
    }

    /**
     * Draws {@code drawCount} candidates and commits none, so the working solution stays as it is
     * and the dataset network stays settled: this measures move generation alone, with no execute,
     * no variable listeners, no settle and no undo in it. Nothing changes, so no pool of null or
     * non-null entities can drain, {@code requiresFlushing()} stays false, and {@code stepStarted}
     * and {@code stepEnded} have nothing to settle - hence no step scope here at all.
     *
     * <p>
     * One iterator for the whole invocation, because one production step also builds one iterator
     * and then draws from it many times. A bi (joined) move iterator retires a left tuple that keeps
     * probing empty, so a later draw in the same invocation picks from a pruned pool and costs less;
     * this is why the draw count is frozen, see
     * {@link AbstractMoveProviderBenchmark#DRAW_ONLY_DRAWS}. Nothing invalidates the data sets
     * either, so they stay warm and real generation can be a little slower than this.
     *
     * @return the last drawn move, kept only so JMH cannot optimize the invocation away
     */
    public Move<Solution_> runDrawOnly(int drawCount, int maxDrawAttemptsPerMove, Blackhole blackhole,
            MovedValueCounter counter) {
        return drawMoves(moveRepository.iterator(), drawCount, maxDrawAttemptsPerMove, blackhole, counter);
    }

    /**
     * Every drawn move is counted into {@code counter}, not only the one a caller goes on to commit,
     * because a step really does generate all of them; see {@link MovedValueCounter}.
     */
    private Move<Solution_> drawMoves(Iterator<Move<Solution_>> moveIterator, int drawCount,
            int maxDrawAttemptsPerMove, Blackhole blackhole, MovedValueCounter counter) {
        Move<Solution_> lastMove = null;
        for (var i = 0; i < drawCount; i++) {
            lastMove = drawMove(moveIterator, maxDrawAttemptsPerMove);
            counter.movedValues += moveProviderCase.countMovedValues(lastMove);
            blackhole.consume(lastMove); // No move is ever thrown away.
        }
        return lastMove;
    }

    /**
     * Retries and never returns null. {@code UniRandomMoveIterator} may assign null to its next-move
     * field, so a null draw is possible; the last move of a step must not be null, or the step does
     * not happen and the benchmark measures the wrong thing.
     */
    private Move<Solution_> drawMove(Iterator<Move<Solution_>> moveIterator, int maxDrawAttemptsPerMove) {
        for (var attempt = 0; attempt < maxDrawAttemptsPerMove; attempt++) {
            if (!moveIterator.hasNext()) {
                continue; // Neighborhoods are never-ending; treat exhaustion as a failed attempt.
            }
            var move = moveIterator.next();
            if (move != null) {
                return move;
            }
        }
        throw new IllegalStateException(
                "The moveProvider (%s) of example (%s) produced no non-null move in (%d) attempts."
                        .formatted(moveProviderCase, example, maxDrawAttemptsPerMove));
    }

    public void tearDownInvocation() {
        maybeFlushConstraintStreamSession();
    }

    /**
     * Bounded periodic flush, not a measurement. Never calling {@code calculateScore()} lets the
     * constraint-stream session's filtered root nodes (installed for any variable that allows
     * unassigned) accumulate one queued tuple per filter flip with nothing draining it -
     * {@code InnerScoreDirector.requiresFlushing()} exists precisely to flag this. Guard against it
     * the same way the enterprise {@code MultiThreadedLocalSearchDecider} already does in production:
     * every {@code FLUSH_EVERY_N_STEPS} invocations, if flushing is required, calculate score once
     * and discard the result.
     */
    private void maybeFlushConstraintStreamSession() {
        if (invocationCounter >= AbstractMoveProviderBenchmark.FLUSH_EVERY_N_STEPS && scoreDirector.requiresFlushing()) {
            scoreDirector.calculateScore();
        }
    }

    public void tearDownIteration() {
        moveRepository.phaseEnded(phaseScope);
    }

    public void teardownTrial() {
        scoreDirector.close();
    }

}
