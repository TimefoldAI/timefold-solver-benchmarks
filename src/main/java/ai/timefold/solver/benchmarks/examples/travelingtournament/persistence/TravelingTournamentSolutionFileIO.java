package ai.timefold.solver.benchmarks.examples.travelingtournament.persistence;

import java.io.File;
import java.util.function.Function;
import java.util.stream.Collectors;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractJsonSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.travelingtournament.domain.Team;
import ai.timefold.solver.benchmarks.examples.travelingtournament.domain.TravelingTournament;

public class TravelingTournamentSolutionFileIO extends
        AbstractJsonSolutionFileIO<ai.timefold.solver.benchmarks.examples.travelingtournament.domain.TravelingTournament> {

    public TravelingTournamentSolutionFileIO() {
        super(ai.timefold.solver.benchmarks.examples.travelingtournament.domain.TravelingTournament.class);
    }

    @Override
    public ai.timefold.solver.benchmarks.examples.travelingtournament.domain.TravelingTournament read(File inputSolutionFile) {
        TravelingTournament travelingTournament = super.read(inputSolutionFile);

        var teamsById = travelingTournament.getTeamList().stream()
                .collect(Collectors.toMap(ai.timefold.solver.benchmarks.examples.travelingtournament.domain.Team::getId,
                        Function.identity()));
        /*
         * Replace the duplicate team instances in the distanceToTeamMap by references to instances from
         * the teamList.
         */
        for (ai.timefold.solver.benchmarks.examples.travelingtournament.domain.Team team : travelingTournament.getTeamList()) {
            var newTravelDistanceMap = deduplicateMap(team.getDistanceToTeamMap(),
                    teamsById, Team::getId);
            team.setDistanceToTeamMap(newTravelDistanceMap);
        }
        return travelingTournament;
    }

}
