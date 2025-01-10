package ai.timefold.solver.benchmarks.examples.meetingscheduling.persistence;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractJsonSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.MeetingSchedule;

public class MeetingSchedulingSolutionFileIO extends AbstractJsonSolutionFileIO<MeetingSchedule> {

    public MeetingSchedulingSolutionFileIO() {
        super(MeetingSchedule.class);
    }

}
