package ai.timefold.solver.benchmarks.competitive.tsplib95;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import ai.timefold.solver.benchmarks.competitive.AbstractCompetitiveBenchmark;
import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;
import ai.timefold.solver.benchmarks.examples.tsp.persistence.TspImporter;
import ai.timefold.solver.core.api.score.buildin.simplelong.SimpleLongScore;

public class Main extends AbstractCompetitiveBenchmark<TSPLIBDataset, TSPLIBConfiguration, TspSolution, SimpleLongScore> {

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        var benchmark = new Main();
        benchmark.run(TSPLIBConfiguration.COMMUNITY_EDITION, TSPLIBConfiguration.COMMUNITY_EDITION_TWEAKED,
                TSPLIBConfiguration.ENTERPRISE_EDITION, TSPLIBDataset.values());
    }

    @Override
    protected String getLibraryName() {
        return "TSPLIB95";
    }

    @Override
    protected SimpleLongScore extractScore(TspSolution tspSolution) {
        return tspSolution.getScore();
    }

    @Override
    protected long extractDistance(SimpleLongScore score) {
        return -score.score();
    }

    @Override
    protected int countLocations(TspSolution tspSolution) {
        return tspSolution.getVisitList().size();
    }

    @Override
    protected int countVehicles(TspSolution tspSolution) {
        return 1;
    }

    @Override
    protected AbstractSolutionImporter<TspSolution> createImporter() {
        return new TspImporter();
    }

}
