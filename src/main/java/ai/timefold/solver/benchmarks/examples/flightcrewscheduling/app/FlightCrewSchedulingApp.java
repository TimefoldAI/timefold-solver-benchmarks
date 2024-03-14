package ai.timefold.solver.benchmarks.examples.flightcrewscheduling.app;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.FlightCrewSolution;
import ai.timefold.solver.benchmarks.examples.flightcrewscheduling.persistence.FlightCrewSchedulingXlsxFileIO;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

public class FlightCrewSchedulingApp
        extends CommonApp<FlightCrewSolution> {

    public static final String SOLVER_CONFIG =
            "ai/timefold/solver/benchmarks/examples/flightcrewscheduling/flightCrewSchedulingSolverConfig.xml";

    public static final String DATA_DIR_NAME = "flightcrewscheduling";

    public static void main(String[] args) {
        var solution = new FlightCrewSchedulingApp().solve("875flights-7days-Europe.xlsx");
        System.out.println("Done: " + solution);
    }

    public FlightCrewSchedulingApp() {
        super("Flight crew scheduling",
                "Assign flights to pilots and flight attendants.",
                SOLVER_CONFIG, DATA_DIR_NAME);
    }

    @Override
    public SolutionFileIO<FlightCrewSolution> createSolutionFileIO() {
        return new FlightCrewSchedulingXlsxFileIO();
    }

}
