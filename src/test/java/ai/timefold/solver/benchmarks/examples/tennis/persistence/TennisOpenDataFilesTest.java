package ai.timefold.solver.benchmarks.examples.tennis.persistence;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.OpenDataFilesTest;
import ai.timefold.solver.benchmarks.examples.tennis.app.TennisApp;
import ai.timefold.solver.benchmarks.examples.tennis.domain.TennisSolution;

class TennisOpenDataFilesTest extends OpenDataFilesTest<TennisSolution> {

    @Override
    protected CommonApp<TennisSolution> createCommonApp() {
        return new TennisApp();
    }
}
