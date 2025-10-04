package ai.timefold.solver.benchmarks.examples.flowshop.persistence;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.OpenDataFilesTest;
import ai.timefold.solver.benchmarks.examples.flowshop.app.FlowShopApp;
import ai.timefold.solver.benchmarks.examples.flowshop.domain.JobScheduleSolution;

class FlowShopTaillardDataFilesTest extends OpenDataFilesTest<JobScheduleSolution> {

    @Override
    protected CommonApp<JobScheduleSolution> createCommonApp() {
        return new FlowShopApp();
    }
}
