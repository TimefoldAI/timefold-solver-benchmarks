package ai.timefold.solver.benchmarks.examples.flightcrewscheduling.persistence;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.OpenDataFilesTest;
import ai.timefold.solver.benchmarks.examples.flightcrewscheduling.app.FlightCrewSchedulingApp;
import ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.FlightCrewSolution;

class FlightCrewSchedulingOpenDataFilesTest extends OpenDataFilesTest<FlightCrewSolution> {

    @Override
    protected CommonApp<FlightCrewSolution> createCommonApp() {
        return new FlightCrewSchedulingApp();
    }
}
