package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicType;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.monitoring.SolverMetric;
import ai.timefold.solver.core.config.solver.random.RandomType;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import ai.timefold.solver.core.impl.constructionheuristic.DefaultConstructionHeuristicPhase;
import ai.timefold.solver.core.impl.constructionheuristic.DefaultConstructionHeuristicPhaseFactory;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.core.impl.heuristic.HeuristicConfigPolicy;
import ai.timefold.solver.core.impl.score.constraint.ConstraintMatchPolicy;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirectorFactory;
import ai.timefold.solver.core.impl.solver.ClassInstanceCache;
import ai.timefold.solver.core.impl.solver.DefaultSolver;
import ai.timefold.solver.core.impl.solver.event.SolverEventSupport;
import ai.timefold.solver.core.impl.solver.random.DefaultRandomFactory;
import ai.timefold.solver.core.impl.solver.recaller.BestSolutionRecallerFactory;
import ai.timefold.solver.core.impl.solver.scope.SolverScope;
import ai.timefold.solver.core.impl.solver.termination.BasicPlumbingTermination;
import ai.timefold.solver.core.impl.solver.termination.TerminationFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XLS-based examples can not read initialized solution from the file.
 * Therefore we have to initialize the solution ourselves, by running the CH on the fastest possible score director.
 */
public final class ProblemInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProblemInitializer.class);

    private static final Map<Example, Object> SOLUTIONS = new EnumMap<>(Example.class);

    public static synchronized <Solution_> Solution_ getSolution(Example example,
            SolutionDescriptor<Solution_> solutionDescriptor,
            Function<ScoreDirectorType, ScoreDirectorFactoryConfig> configFunction, Supplier<Solution_> solutionSupplier) {
        var fastestPossibleScoreDirectorType = Arrays.stream(ScoreDirectorType.values())
                .filter(example::isSupportedOn)
                .max(ScoreDirectorType::compareTo)
                .orElseThrow();
        var config = configFunction.apply(fastestPossibleScoreDirectorType);
        var scoreDirectorFactory = ScoreDirectorType.buildScoreDirectorFactory(config, solutionDescriptor);
        return (Solution_) SOLUTIONS.computeIfAbsent(example,
                e -> initialize(example, solutionSupplier.get(), scoreDirectorFactory));
    }

    private static <Solution_> Solution_ initialize(Example example, Solution_ uninitializedSolution,
            InnerScoreDirectorFactory<Solution_, ?> scoreDirectorFactory) {
        try (var scoreDirector = scoreDirectorFactory.buildScoreDirector(false, ConstraintMatchPolicy.DISABLED)) {
            scoreDirector.setWorkingSolution(uninitializedSolution);
            scoreDirector.triggerVariableListeners();
            var score = scoreDirector.calculateScore();
            if (score.isSolutionInitialized()) { // No need to do anything.
                LOGGER.info("Example {} already initialized.", example);
                return uninitializedSolution;
            }
            LOGGER.info("Initializing example {}.", example);
            // Configure the construction heuristic.
            var config = new ConstructionHeuristicPhaseConfig()
                    .withConstructionHeuristicType(ConstructionHeuristicType.FIRST_FIT);
            var factory = new DefaultConstructionHeuristicPhaseFactory<Solution_>(config);
            var policy = new HeuristicConfigPolicy.Builder<Solution_>()
                    .withEnvironmentMode(EnvironmentMode.REPRODUCIBLE)
                    .withRandom(new Random(0))
                    .withInitializingScoreTrend(scoreDirectorFactory.getInitializingScoreTrend())
                    .withSolutionDescriptor(scoreDirectorFactory.getSolutionDescriptor())
                    .withClassInstanceCache(ClassInstanceCache.create())
                    .build();
            var bestSolutionRecaller =
                    BestSolutionRecallerFactory.create().<Solution_> buildBestSolutionRecaller(EnvironmentMode.REPRODUCIBLE);
            bestSolutionRecaller.setSolverEventSupport(new SolverEventSupport<>(null));
            var termination = TerminationFactory.<Solution_> create(new TerminationConfig())
                    .buildTermination(policy, new BasicPlumbingTermination<>(false));
            var constructionHeuristicPhase = (DefaultConstructionHeuristicPhase<Solution_>) factory.buildPhase(0, false,
                    policy, bestSolutionRecaller, termination);

            // Create solver; more or less a mock.
            var solverScope = new SolverScope<Solution_>();
            solverScope.setBestSolution(uninitializedSolution);
            solverScope.setScoreDirector(scoreDirector);
            solverScope.setSolverMetricSet(EnumSet.noneOf(SolverMetric.class));
            solverScope.startingNow();
            var solver = new DefaultSolver<>(EnvironmentMode.REPRODUCIBLE, new DefaultRandomFactory(RandomType.JDK, 0L),
                    bestSolutionRecaller, null, new BasicPlumbingTermination<>(false), List.of(constructionHeuristicPhase),
                    solverScope, null);
            constructionHeuristicPhase.setSolver(solver);

            // Start the construction heuristic.
            bestSolutionRecaller.solvingStarted(solverScope);
            constructionHeuristicPhase.solvingStarted(solverScope);
            constructionHeuristicPhase.solve(solverScope);
            constructionHeuristicPhase.solvingEnded(solverScope);
            bestSolutionRecaller.solvingEnded(solverScope);
            solverScope.endingNow();
            if (!scoreDirector.calculateScore().isSolutionInitialized()) {
                throw new IllegalStateException("Impossible state: uninitialized after the end of CH.");
            }
            LOGGER.info("Example {} initialized.", example);
            return solverScope.getBestSolution();
        }
    }

}
