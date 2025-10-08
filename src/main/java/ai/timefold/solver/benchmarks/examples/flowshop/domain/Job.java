package ai.timefold.solver.benchmarks.examples.flowshop.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowSources;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Job.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@PlanningEntity
public class Job {

    @PlanningId
    private int id;
    @JsonIdentityReference(alwaysAsId = true)
    private Machine[] allMachines;
    @InverseRelationShadowVariable(sourceVariableName = "jobs")
    @JsonIgnore
    private Machine machine;
    @PreviousElementShadowVariable(sourceVariableName = "jobs")
    @JsonIgnore
    private Job previousJob;
    @ShadowVariable(supplierName = "updateStartTime")
    @JsonIgnore
    private JobMachineTime start;
    @ShadowVariable(supplierName = "updateEndTime")
    @JsonIgnore
    private JobMachineTime end;
    private int processTimeSum = 0;

    public Job() {
    }

    public Job(int id, Machine[] allMachines) {
        this.id = id;
        this.allMachines = allMachines;
        for (var allMachine : allMachines) {
            processTimeSum += allMachine.getProcessTime(id);
        }
    }

    public int getId() {
        return id;
    }

    public Machine[] getAllMachines() {
        return allMachines;
    }

    public Job getPreviousJob() {
        return previousJob;
    }

    public void setPreviousJob(Job previousJob) {
        this.previousJob = previousJob;
    }

    public JobMachineTime getStart() {
        return start;
    }

    public void setStart(JobMachineTime start) {
        this.start = start;
    }

    public JobMachineTime getEnd() {
        return end;
    }

    public void setEnd(JobMachineTime end) {
        this.end = end;
    }

    public int getProcessingTimeSum() {
        return processTimeSum;
    }

    public int getProcessingTime(int machineId) {
        return allMachines[machineId].getProcessTime(id);
    }

    @JsonIgnore
    @ShadowSources(value = { "previousJob.end", "machine" })
    public JobMachineTime updateStartTime() {
        if (machine == null) {
            return null;
        }
        var newStartTime = new JobMachineTime(allMachines.length);
        // A machine can perform only one job at a time,
        // and a job can only start on one machine after finishing the process at the previous machine.
        // The completion time of this job in the first machine depends only on the previous job completion time.
        // It can only start after the previous job is completed.
        var previousMachineTime = newStartTime.setTime(0, getPreviousEnd(0));
        for (var i = 1; i < allMachines.length; i++) {
            // The job execution for the following machines relies on the completion time of either the previous job
            // or the previous machine,
            // depending on which is greater. 
            // That way, the job can only begin on the machine once it has completed on the previous machine
            // or after the prior job has finished.
            previousMachineTime = newStartTime.setTime(i, Math.max(getPreviousEnd(i), previousMachineTime));
        }
        return newStartTime;
    }

    @JsonIgnore
    @ShadowSources(value = { "start" })
    public JobMachineTime updateEndTime() {
        if (start == null) {
            return null;
        }
        var newEndTime = new JobMachineTime(allMachines.length);
        var previousMachineTime = 0;
        for (var i = 0; i < allMachines.length; i++) {
            previousMachineTime = newEndTime.setTime(i, Math.max(previousMachineTime, start.getTime(i)) + allMachines[i].getProcessTime(id));
        }
        return newEndTime;
    }

    @JsonIgnore
    private int getPreviousEnd(int machineId) {
        if (previousJob != null) {
            return previousJob.getEnd(machineId);
        }
        return 0;
    }

    @JsonIgnore
    public int getEnd(int machineId) {
        if (end == null) {
            return 0;
        }
        return end.getTime(machineId);
    }

    @JsonIgnore
    public int getJobEndTime() {
        if (end == null) {
            return 0;
        }
        return end.getLastMachineTime();
    }

    @Override
    public String toString() {
        return "Job " + id;
    }
}
