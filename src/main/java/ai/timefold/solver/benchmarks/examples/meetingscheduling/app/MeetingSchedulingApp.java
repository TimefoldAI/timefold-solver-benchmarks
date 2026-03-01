package ai.timefold.solver.benchmarks.examples.meetingscheduling.app;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.MeetingSchedule;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.persistence.MeetingSchedulingSolutionFileIO;
import ai.timefold.solver.core.api.domain.solution.SolutionFileIO;

public class MeetingSchedulingApp extends CommonApp<MeetingSchedule> {

    public static final String SOLVER_CONFIG =
            "ai/timefold/solver/benchmarks/examples/meetingscheduling/meetingSchedulingSolverConfig.xml";

    public static final String DATA_DIR_NAME = "meetingscheduling";

    public static void main(String[] args) {
        var solution = new MeetingSchedulingApp().solve("100meetings-320timegrains-5rooms.json");
        System.out.println("Done: " + solution);
    }

    public MeetingSchedulingApp() {
        super("Meeting scheduling",
                "Assign meetings a starting time and a room.",
                SOLVER_CONFIG, DATA_DIR_NAME);
    }

    @Override
    public SolutionFileIO<MeetingSchedule> createSolutionFileIO() {
        return new MeetingSchedulingSolutionFileIO();
    }

}
