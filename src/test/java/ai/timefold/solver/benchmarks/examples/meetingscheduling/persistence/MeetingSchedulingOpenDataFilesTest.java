package ai.timefold.solver.benchmarks.examples.meetingscheduling.persistence;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.OpenDataFilesTest;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.app.MeetingSchedulingApp;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.MeetingSchedule;

class MeetingSchedulingOpenDataFilesTest extends OpenDataFilesTest<MeetingSchedule> {

    @Override
    protected CommonApp<MeetingSchedule> createCommonApp() {
        return new MeetingSchedulingApp();
    }
}
