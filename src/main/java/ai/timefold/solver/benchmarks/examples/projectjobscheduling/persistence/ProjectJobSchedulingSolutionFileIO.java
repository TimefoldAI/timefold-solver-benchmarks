package ai.timefold.solver.benchmarks.examples.projectjobscheduling.persistence;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractJsonSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.projectjobscheduling.domain.Schedule;

public class ProjectJobSchedulingSolutionFileIO
        extends AbstractJsonSolutionFileIO<Schedule> {

    public ProjectJobSchedulingSolutionFileIO() {
        super(Schedule.class);
    }
}
