package ai.timefold.solver.benchmarks.examples.cloudbalancing.persistence;

import ai.timefold.solver.benchmarks.examples.cloudbalancing.app.CloudBalancingApp;
import ai.timefold.solver.benchmarks.examples.cloudbalancing.domain.CloudBalance;
import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.OpenDataFilesTest;

class CloudBalancingOpenDataFilesTest extends OpenDataFilesTest<CloudBalance> {

    @Override
    protected CommonApp<CloudBalance> createCommonApp() {
        return new CloudBalancingApp();
    }
}
