package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.api.domain.solution.SolutionFileIO;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.phase.PhaseConfig;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.core.impl.heuristic.HeuristicConfigPolicy;
import ai.timefold.solver.core.impl.localsearch.DefaultLocalSearchPhase;
import ai.timefold.solver.core.impl.localsearch.decider.LocalSearchDecider;
import ai.timefold.solver.core.impl.localsearch.scope.LocalSearchPhaseScope;
import ai.timefold.solver.core.impl.localsearch.scope.LocalSearchStepScope;
import ai.timefold.solver.core.impl.neighborhood.MoveRepository;
import ai.timefold.solver.core.impl.phase.PhaseFactory;
import ai.timefold.solver.core.impl.score.constraint.ConstraintMatchPolicy;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;
import ai.timefold.solver.core.impl.score.director.ScoreDirectorFactory;
import ai.timefold.solver.core.impl.solver.ClassInstanceCache;
import ai.timefold.solver.core.impl.solver.DefaultSolverFactory;
import ai.timefold.solver.core.impl.solver.random.RandomSource;
import ai.timefold.solver.core.impl.solver.recaller.BestSolutionRecallerFactory;
import ai.timefold.solver.core.impl.solver.scope.SolverScope;
import ai.timefold.solver.core.impl.solver.termination.BasicPlumbingTermination;
import ai.timefold.solver.core.impl.solver.termination.SolverTermination;
import ai.timefold.solver.core.impl.solver.termination.TerminationFactory;
import ai.timefold.solver.core.preview.api.move.Move;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractProblem<Solution_> implements Problem {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private static final double MOVES_BEFORE_UNDO = 10;

    private final Example example;
    private final SolutionDescriptor<Solution_> solutionDescriptor;
    private final ScoreDirectorType scoreDirectorType;
    private final ScoreDirectorFactory<Solution_, ?> scoreDirectorFactory;
    private final Solution_ originalSolution;

    private InnerScoreDirector<Solution_, ?> scoreDirector;
    private MoveRepository<Solution_> moveRepository;
    private Iterator<Move<Solution_>> moveIterator;
    private LocalSearchPhaseScope<Solution_> phaseScope;
    private LocalSearchStepScope<Solution_> stepScope;
    private Move<Solution_> move;

    private long invocationCount = 0;
    private boolean willUndo = true;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected AbstractProblem(final Example example, final ScoreDirectorType scoreDirectorType) {
        this.example = Objects.requireNonNull(example);
        this.scoreDirectorType = Objects.requireNonNull(scoreDirectorType);
        var solverConfig = buildSolverConfig(scoreDirectorType);
        this.solutionDescriptor = ((DefaultSolverFactory) SolverFactory.create(solverConfig)).getSolutionDescriptor();
        this.scoreDirectorFactory =
                ScoreDirectorType.buildScoreDirectorFactory(solverConfig.getScoreDirectorFactoryConfig(), solutionDescriptor);
        this.originalSolution = readOriginalSolution();
    }

    protected final ScoreDirectorFactoryConfig buildInitialScoreDirectorFactoryConfig() {
        return switch (scoreDirectorType) {
            case CONSTRAINT_STREAMS, CONSTRAINT_STREAMS_JUSTIFIED -> new ScoreDirectorFactoryConfig()
                    .withConstraintStreamAutomaticNodeSharing(true);
        };
    }

    abstract protected SolverConfig buildSolverConfig(ScoreDirectorType scoreDirectorType);

    private Solution_ readOriginalSolution() {
        var directoryName = this.example.getDirectoryName();
        var solutionFilename = "data/%s/%s-%s.json".formatted(directoryName, directoryName, getDatasetName());
        var solutionFile = new File(solutionFilename);
        if (!solutionFile.exists()) {
            throw new IllegalStateException("Solution file not found: " + solutionFile.getAbsolutePath());
        }
        var solutionFileIO = createSolutionFileIO();
        while (true) {
            try {
                return solutionFileIO.read(solutionFile);
            } catch (StackOverflowError error) { // For some reason, TSP deserialization overflows here *once in a while*.
                LOGGER.debug("Jackson's thrown stack overflow, retrying.");
            }
        }
    }

    abstract protected SolutionFileIO<Solution_> createSolutionFileIO();

    abstract protected String getDatasetName();

    protected MoveRepository<Solution_> buildMoveRepository(SolutionDescriptor<Solution_> solutionDescriptor) {
        // Build the top-level local search move selector as the solver would've built it.
        // Deliberately not going through SolverFactory.create(SolverConfig): that would build its own,
        // separate SolutionDescriptor, and the resulting selectors would end up bound to a different
        // ListVariableDescriptor instance than the real score director's canonical one. Shadow variable
        // notifications are dispatched to the score director's own canonical supply only, so a selector
        // holding a foreign descriptor would silently stop receiving updates after its first snapshot.
        var configPolicyBuilder = new HeuristicConfigPolicy.Builder<Solution_>()
                .withPreviewFeatureSet(Set.of())
                .withEnvironmentMode(EnvironmentMode.PHASE_ASSERT)
                .withRandom(RandomSource.seeded(0))
                .withInitializingScoreTrend(scoreDirectorFactory.getInitializingScoreTrend())
                .withSolutionDescriptor(solutionDescriptor)
                .withClassInstanceCache(ClassInstanceCache.create());
        this.example.getNearbyDistanceMeter().ifPresent(configPolicyBuilder::withNearbyDistanceMeterClass);
        var configPolicy = configPolicyBuilder.build();

        var basicPlumbingTermination = new BasicPlumbingTermination<Solution_>(false);
        SolverTermination<Solution_> termination = TerminationFactory.<Solution_> create(new TerminationConfig())
                .buildTermination(configPolicy, basicPlumbingTermination);
        var bestSolutionRecaller =
                BestSolutionRecallerFactory.create().<Solution_> buildBestSolutionRecaller(EnvironmentMode.PHASE_ASSERT);

        var phaseList = PhaseFactory.<Solution_> buildPhases(List.<PhaseConfig> of(new LocalSearchPhaseConfig()),
                configPolicy, bestSolutionRecaller, termination);
        var localSearchPhase = (DefaultLocalSearchPhase<Solution_>) phaseList.getLast();
        try { // Decider is not accessible. Hack our way in.
            var deciderField = Stream.of(DefaultLocalSearchPhase.class.getDeclaredFields())
                    .filter(f -> f.getName().equals("decider"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("MoveSelectorFactory field not found"));
            deciderField.setAccessible(true);
            var decider = (LocalSearchDecider<Solution_>) deciderField.get(localSearchPhase);
            return decider.getMoveRepository();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to extract MoveSelector from LocalSearchPhase", e);
        }
    }

    @Override
    public final void setupTrial() {
        var constraintMatchPolicy =
                scoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS_JUSTIFIED ? ConstraintMatchPolicy.ENABLED
                        : ConstraintMatchPolicy.DISABLED;
        moveRepository = buildMoveRepository(solutionDescriptor);
        scoreDirector = scoreDirectorFactory.createScoreDirectorBuilder()
                .withConstraintMatchPolicy(constraintMatchPolicy)
                .build();
    }

    @Override
    public final void setupIteration() {
        // We only care about incremental performance; therefore calculate the entire solution outside of invocation.
        scoreDirector.setWorkingSolution(scoreDirector.cloneSolution(originalSolution)); // Use fresh solution again.
        scoreDirector.updateShadowVariables();
        scoreDirector.calculateScore();
        // Prepare the lifecycle.
        var solverScope = new SolverScope<Solution_>();
        solverScope.setScoreDirector(scoreDirector);
        solverScope.setWorkingRandom(RandomSource.seeded(0)); // Fully reproducible random selection.
        moveRepository.solvingStarted(solverScope);
        phaseScope = new LocalSearchPhaseScope<>(solverScope, 0);
        moveRepository.phaseStarted(phaseScope);
    }

    @Override
    public final void setupInvocation() {
        if (stepScope == null) {
            stepScope = new LocalSearchStepScope<>(phaseScope);
            moveRepository.stepStarted(stepScope);
            moveIterator = moveRepository.iterator();
        }
        // Only undo every nth move; undo means the end of the step.
        willUndo = (invocationCount % MOVES_BEFORE_UNDO) < (MOVES_BEFORE_UNDO - 1);
        move = moveIterator.next();
    }

    /**
     * Designed to emulate the solver.
     * The solver typically tries all sorts of moves, calculates their score, and undoes them.
     * After a certain amount of such moves, it picks one move and that move is finally not undone.
     * So we do the same here, and every {@link #MOVES_BEFORE_UNDO}th move will be undone.
     *
     * <p>
     * We're benchmarking the actual operations inside the score director:
     *
     * <ul>
     * <li>Speed of variable updates.</li>
     * <li>Speed of score calculation on those updates.</li>
     * </ul>
     *
     * <p>
     * Unfortunately, we also benchmark a bit of the overhead of the move. Hopefully, that is not too much.
     * More importantly, it is a constant overhead and therefore should not affect the results.
     *
     * @return in order to prevent results from being optimized away
     */
    @Override
    public final Object runInvocation() {
        if (willUndo) {
            return scoreDirector.executeTemporaryMove(move, false);
        } else {
            scoreDirector.executeMove(move);
            return scoreDirector.calculateScore();
        }
    }

    @Override
    public final void tearDownInvocation() {
        if (!willUndo) { // Move was not undone; this signifies the end of the step.
            endStep();
        }
        invocationCount++;
    }

    private void endStep() {
        moveRepository.stepEnded(stepScope);
        stepScope = null;
    }

    @Override
    public final void tearDownIteration() {
        if (stepScope != null) { // Clean up in case the last move was undone.
            endStep();
        }
        invocationCount = 0;
    }

    @Override
    public final void teardownTrial() {
        scoreDirector.close();
        // No need to do anything.
    }

}
