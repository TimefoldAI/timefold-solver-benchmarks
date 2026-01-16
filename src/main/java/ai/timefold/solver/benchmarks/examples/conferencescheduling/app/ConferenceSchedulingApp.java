
package ai.timefold.solver.benchmarks.examples.conferencescheduling.app;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceSolution;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.persistence.ConferenceSchedulingSolutionFileIO;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

public class ConferenceSchedulingApp
        extends CommonApp<ConferenceSolution> {

    public static final String SOLVER_CONFIG =
            "ai/timefold/solver/benchmarks/examples/conferencescheduling/conferenceSchedulingSolverConfig.xml";

    public static final String DATA_DIR_NAME = "conferencescheduling";

    public static void main(String[] args) {
        var app = new ConferenceSchedulingApp();
        var solution = app.solve("216talks-18timeslots-20rooms.json");
        System.out.println("Done: " + solution);
    }

    public ConferenceSchedulingApp() {
        super("Conference scheduling",
                "Assign conference talks to a timeslot and a room.",
                SOLVER_CONFIG, DATA_DIR_NAME);
    }

    @Override
    public SolutionFileIO<ConferenceSolution> createSolutionFileIO() {
        return new ConferenceSchedulingSolutionFileIO();
    }

}
