package ai.timefold.solver.benchmarks.examples.travelingtournament.domain;

import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.AbstractKeyDeserializer;

final class TeamKeyDeserializer extends AbstractKeyDeserializer<Team> {

    public TeamKeyDeserializer() {
        super(Team.class);
    }

    @Override
    protected Team createInstance(long id) {
        return new Team(id);
    }
}
