package ai.timefold.solver.benchmarks.micro.scoredirector.problems;

import java.util.Objects;

import ai.timefold.solver.benchmarks.examples.travelingtournament.domain.Match;
import ai.timefold.solver.benchmarks.examples.travelingtournament.domain.TravelingTournament;
import ai.timefold.solver.benchmarks.examples.travelingtournament.persistence.TravelingTournamentSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.travelingtournament.score.TravelingTournamentConstraintProvider;
import ai.timefold.solver.benchmarks.micro.scoredirector.Example;
import ai.timefold.solver.benchmarks.micro.scoredirector.ScoreDirectorType;
import ai.timefold.solver.core.api.domain.solution.SolutionFileIO;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;

public final class TravelingTournamentProblem extends AbstractProblem<TravelingTournament> {

    public TravelingTournamentProblem(ScoreDirectorType scoreDirectorType) {
        super(Example.TRAVELING_TOURNAMENT, scoreDirectorType);
    }

    private ScoreDirectorFactoryConfig buildScoreDirectorFactoryConfig(ScoreDirectorType scoreDirectorType) {
        var scoreDirectorFactoryConfig = buildInitialScoreDirectorFactoryConfig();
        var nonNullScoreDirectorType = Objects.requireNonNull(scoreDirectorType);
        if (nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS
                || nonNullScoreDirectorType == ScoreDirectorType.CONSTRAINT_STREAMS_JUSTIFIED) {
            return scoreDirectorFactoryConfig.withConstraintProviderClass(TravelingTournamentConstraintProvider.class);
        }
        throw new UnsupportedOperationException("Score director: " + scoreDirectorType);
    }

    @Override
    protected SolverConfig buildSolverConfig(ScoreDirectorType scoreDirectorType) {
        return new SolverConfig()
                .withSolutionClass(TravelingTournament.class)
                .withEntityClasses(Match.class)
                .withScoreDirectorFactory(buildScoreDirectorFactoryConfig(scoreDirectorType));
    }

    @Override
    protected SolutionFileIO<TravelingTournament> createSolutionFileIO() {
        return new TravelingTournamentSolutionFileIO();
    }

    @Override
    protected String getDatasetName() {
        return "4-super14";
    }

}
