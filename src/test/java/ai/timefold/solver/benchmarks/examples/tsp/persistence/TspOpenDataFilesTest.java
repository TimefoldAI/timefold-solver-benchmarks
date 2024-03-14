package ai.timefold.solver.benchmarks.examples.tsp.persistence;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.OpenDataFilesTest;
import ai.timefold.solver.benchmarks.examples.tsp.app.TspApp;
import ai.timefold.solver.benchmarks.examples.tsp.domain.TspSolution;

class TspOpenDataFilesTest extends OpenDataFilesTest<TspSolution> {

    @Override
    protected CommonApp<TspSolution> createCommonApp() {
        return new TspApp();
    }
}
