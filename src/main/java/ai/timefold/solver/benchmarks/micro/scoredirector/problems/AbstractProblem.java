package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import java.io.File;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import ai.timefold.solver.core.api.score.calculator.EasyScoreCalculator;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.core.impl.heuristic.move.Move;
import ai.timefold.solver.core.impl.heuristic.selector.move.MoveSelector;
import ai.timefold.solver.core.impl.localsearch.DefaultLocalSearchPhase;
import ai.timefold.solver.core.impl.localsearch.decider.LocalSearchDecider;
import ai.timefold.solver.core.impl.localsearch.scope.LocalSearchPhaseScope;
import ai.timefold.solver.core.impl.localsearch.scope.LocalSearchStepScope;
import ai.timefold.solver.core.impl.move.director.MoveDirector;
import ai.timefold.solver.core.impl.score.constraint.ConstraintMatchPolicy;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirectorFactory;
import ai.timefold.solver.core.impl.solver.DefaultSolver;
import ai.timefold.solver.core.impl.solver.scope.SolverScope;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractProblem<Solution_> implements Problem {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private static final double MOVES_BEFORE_UNDO = 10;

    private final Example example;
    private final SolutionDescriptor<Solution_> solutionDescriptor;
    private final ScoreDirectorType scoreDirectorType;
    private final InnerScoreDirectorFactory<Solution_, ?> scoreDirectorFactory;
    private final Solution_ originalSolution;

    private InnerScoreDirector<Solution_, ?> scoreDirector;
    private MoveDirector<Solution_> moveDirector;
    private MoveSelector<Solution_> moveSelector;
    private Iterator<Move<Solution_>> moveIterator;
    private LocalSearchPhaseScope<Solution_> phaseScope;
    private LocalSearchStepScope<Solution_> stepScope;
    private Move<Solution_> move;

    private long invocationCount = 0;
    private boolean willUndo = true;

    protected AbstractProblem(final Example example, final ScoreDirectorType scoreDirectorType) {
        this.example = Objects.requireNonNull(example);
        this.scoreDirectorType = Objects.requireNonNull(scoreDirectorType);
        this.solutionDescriptor = buildSolutionDescriptor();
        var scoreDirectorFactoryConfig = buildScoreDirectorFactoryConfig(scoreDirectorType);
        this.scoreDirectorFactory = ScoreDirectorType.buildScoreDirectorFactory(scoreDirectorFactoryConfig, solutionDescriptor);
        this.originalSolution = readOriginalSolution();
    }

    protected final ScoreDirectorFactoryConfig buildInitialScoreDirectorFactoryConfig() {
        return switch (scoreDirectorType) {
            case CONSTRAINT_STREAMS, CONSTRAINT_STREAMS_JUSTIFIED -> new ScoreDirectorFactoryConfig()
                    .withConstraintStreamAutomaticNodeSharing(true);
            default -> new ScoreDirectorFactoryConfig();
        };
    }

    abstract protected ScoreDirectorFactoryConfig buildScoreDirectorFactoryConfig(ScoreDirectorType scoreDirectorType);

    abstract protected SolutionDescriptor<Solution_> buildSolutionDescriptor();

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

    protected MoveSelector<Solution_> buildMoveSelector(SolutionDescriptor<Solution_> solutionDescriptor) {
        // Build the top-level local search move selector as the solver would've built it.
        var solverConfig = new SolverConfig()
                .withEnvironmentMode(EnvironmentMode.REPRODUCIBLE)
                .withSolutionClass(solutionDescriptor.getSolutionClass())
                .withEntityClasses(solutionDescriptor.getEntityClassSet().toArray(new Class[0]))
                .withEasyScoreCalculatorClass(DummyEasyScoreCalculator.class);
        this.example.getNearbyDistanceMeter().ifPresent(solverConfig::setNearbyDistanceMeterClass);
        var solver = (DefaultSolver<Solution_>) SolverFactory.create(solverConfig)
                .buildSolver();
        var localSearchPhase = (DefaultLocalSearchPhase<Solution_>) solver.getPhaseList().getLast();
        try { // Decider is not accessible. Hack our way in.
            var deciderField = Stream.of(DefaultLocalSearchPhase.class.getDeclaredFields())
                    .filter(f -> f.getName().equals("decider"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("MoveSelectorFactory field not found"));
            deciderField.setAccessible(true);
            var decider = (LocalSearchDecider<Solution_>) deciderField.get(localSearchPhase);
            return decider.getMoveSelector();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to extract MoveSelector from LocalSearchPhase", e);
        }
    }

    @Override
    public final void setupTrial() {
        var constraintMatchPolicy =
                scoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS_JUSTIFIED ? ConstraintMatchPolicy.ENABLED
                        : ConstraintMatchPolicy.DISABLED;
        scoreDirector = scoreDirectorFactory.buildScoreDirector(false, constraintMatchPolicy);
        moveDirector = new MoveDirector<>(scoreDirector);
        moveSelector = buildMoveSelector(solutionDescriptor);
    }

    @Override
    public final void setupIteration() {
        // We only care about incremental performance; therefore calculate the entire solution outside of invocation.
        scoreDirector.setWorkingSolution(scoreDirector.cloneSolution(originalSolution)); // Use fresh solution again.
        scoreDirector.triggerVariableListeners();
        scoreDirector.calculateScore();
        // Prepare the lifecycle.
        var solverScope = new SolverScope<Solution_>();
        solverScope.setScoreDirector(scoreDirector);
        solverScope.setWorkingRandom(new Random(0)); // Fully reproducible random selection.
        moveSelector.solvingStarted(solverScope);
        phaseScope = new LocalSearchPhaseScope<>(solverScope, 0);
        moveSelector.phaseStarted(phaseScope);
    }

    @Override
    public final void setupInvocation() {
        if (stepScope == null) {
            stepScope = new LocalSearchStepScope<>(phaseScope);
            moveSelector.stepStarted(stepScope);
            moveIterator = moveSelector.iterator();
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
            try (var ephemeralMoveDirector = moveDirector.ephemeral()) {
                move.doMoveOnly(ephemeralMoveDirector.getScoreDirector());
            }
        } else {
            move.doMoveOnly(scoreDirector); // Do the move without any undo.
        }
        return scoreDirector.calculateScore();
    }

    @Override
    public final void tearDownInvocation() {
        if (!willUndo) { // Move was not undone; this signifies the end of the step.
            endStep();
        }
        invocationCount++;
    }

    private void endStep() {
        moveSelector.stepEnded(stepScope);
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

    public static final class DummyEasyScoreCalculator<Solution_> implements EasyScoreCalculator<Solution_, SimpleScore> {

        public DummyEasyScoreCalculator() { // No-arg constructor required.
        }

        @Override
        public SimpleScore calculateScore(Solution_ solution) {
            return SimpleScore.ZERO;
        }
    }

}
