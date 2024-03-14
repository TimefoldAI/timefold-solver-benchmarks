package ai.timefold.solver.benchmarks.examples.projectjobscheduling.domain.solver;

import ai.timefold.solver.benchmarks.examples.projectjobscheduling.domain.Allocation;
import ai.timefold.solver.benchmarks.examples.projectjobscheduling.domain.JobType;
import ai.timefold.solver.benchmarks.examples.projectjobscheduling.domain.Schedule;
import ai.timefold.solver.core.api.domain.entity.PinningFilter;

public class NotSourceOrSinkAllocationFilter implements PinningFilter<Schedule, Allocation> {

    @Override
    public boolean accept(Schedule schedule, Allocation allocation) {
        JobType jobType = allocation.getJob().getJobType();
        return jobType == JobType.SOURCE || jobType == JobType.SINK;
    }

}
