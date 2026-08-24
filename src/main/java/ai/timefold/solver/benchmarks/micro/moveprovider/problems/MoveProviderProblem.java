package ai.timefold.solver.benchmarks.micro.moveprovider.problems;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.benchmarks.micro.moveprovider.Example;
import ai.timefold.solver.benchmarks.micro.moveprovider.MoveProviderCase;
import ai.timefold.solver.benchmarks.micro.moveprovider.jmh.AbstractMoveProviderBenchmark;
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
 * Runs one {@link MoveProviderCase} through the two scenarios described in
 * {@code /home/agent/.claude/plans/zany-drifting-ocean.md}: a fixed number of steps, each drawing a
 * fixed number of moves, only the last of which is committed.
 *
 * <p>
 * <b>Drift is bounded at the iteration, not the invocation.</b> {@link #setupIteration()} starts
 * over every time: a fresh clone of the original solution, a fresh {@link RandomSource#seeded(long)
 * seeded(0)}, and a fresh {@link NeighborhoodsBasedMoveRepository}. So every iteration replays the
 * same draws from the same starting point, and only machine noise varies between iterations.
 * Invocations inside one iteration do drift, because each one commits {@code movesPerStep} moves
 * that are never undone — that is accepted; JMH forks are independent JVMs regardless. Do not add a
 * per-invocation reset: a per-invocation clone would cost more than the thing being measured.
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
    private long stepCounter;

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

    @SuppressWarnings("unchecked")
    public void setupTrial() {
        moveStreamFactory = new DefaultMoveStreamFactory<>(solutionDescriptor, EnvironmentMode.NO_ASSERT);
        moveProvider = (MoveProvider<Solution_>) moveProviderCase.createMoveProvider(solutionDescriptor.getMetaModel());
        scoreDirector = scoreDirectorFactory.createScoreDirectorBuilder()
                .withConstraintMatchPolicy(ConstraintMatchPolicy.DISABLED)
                .build();
    }

    public void setupIteration() {
        // We only care about incremental performance; therefore calculate the entire solution outside of invocation.
        scoreDirector.setWorkingSolution(scoreDirector.cloneSolution(originalSolution));
        scoreDirector.updateShadowVariables();
        scoreDirector.calculateScore();
        // Prepare the lifecycle. A fresh repository every iteration: its dataset session binds to
        // this iteration's working solution.
        moveRepository = new NeighborhoodsBasedMoveRepository<>(moveStreamFactory, List.of(moveProvider));
        var solverScope = new SolverScope<Solution_>();
        solverScope.setScoreDirector(scoreDirector);
        solverScope.setWorkingRandom(RandomSource.seeded(0)); // Fully reproducible random selection.
        moveRepository.solvingStarted(solverScope);
        phaseScope = new LocalSearchPhaseScope<>(solverScope, 0);
        moveRepository.phaseStarted(phaseScope); // Also calls scoreDirector.setMoveRepository(moveRepository).
        stepCounter = 0;
    }

    public void setupInvocation() {

    }

    /**
     * {@code calculateScore()} is deliberately never called here. The neighborhood dataset network
     * (Bavet, behind {@code MoveStreamFactory}/{@code NeighborhoodsBasedMoveRepository}) and the
     * constraint-stream scoring network are two fully independent Bavet sessions with no shared
     * settle point: {@code AbstractScoreDirector.afterVariableChanged} fans out to both, but each
     * session has its own root nodes, {@code tupleMap}, {@code settled} flag and propagation queue.
     * Only layer-0 root-node bookkeeping happens eagerly on every {@code executeMove()}; everything
     * past it — joins, {@code groupBy}, {@code ifExists}, score aggregation — happens only inside
     * {@code settle()}. The dataset network is settled by {@code moveRepository.stepStarted}/
     * {@code stepEnded} regardless of whether score is ever calculated; the constraint-stream
     * network is settled only inside {@code calculateScore()}. So calling it here would trigger a
     * network this benchmark is not measuring, and its cost — dominated by constraint-stream joins
     * on the larger datasets — would swamp the dataset-network signal this benchmark exists to
     * isolate.
     *
     * @return the last committed move, kept only so JMH cannot optimize the invocation away
     */
    public Move<Solution_> runInvocation(int stepCount, int movesPerStep, int maxDrawAttemptsPerMove, Blackhole blackhole) {
        Move<Solution_> lastMove = null;
        for (var step = 0; step < stepCount; step++) {
            var stepScope = new LocalSearchStepScope<>(phaseScope, step);
            moveRepository.stepStarted(stepScope);
            var moveIterator = moveRepository.iterator();
            for (var i = 0; i < movesPerStep; i++) {
                lastMove = drawMove(moveIterator, maxDrawAttemptsPerMove);
                blackhole.consume(lastMove); // No move is ever thrown away.
            }
            // The step must happen; drawMove() has already guaranteed a non-null move.
            scoreDirector.executeMove(lastMove);
            moveRepository.stepEnded(stepScope); // Settles the dataset network; this is what scenario 2 measures.
            stepCounter++;
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
     * Bounded periodic flush, not a measurement. Skipping {@code calculateScore()} forever lets the
     * constraint-stream session's filtered root nodes (installed for any variable that allows
     * unassigned) accumulate one queued tuple per filter flip with nothing draining it —
     * {@code InnerScoreDirector.requiresFlushing()} exists precisely to flag this. Guard against it
     * the same way the enterprise {@code MultiThreadedLocalSearchDecider} already does in production:
     * every {@code FLUSH_EVERY_N_STEPS} steps, if flushing is required, calculate score once and
     * discard the result.
     */
    private void maybeFlushConstraintStreamSession() {
        if (stepCounter >= AbstractMoveProviderBenchmark.FLUSH_EVERY_N_STEPS && scoreDirector.requiresFlushing()) {
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
