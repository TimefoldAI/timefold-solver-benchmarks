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
    @ShadowVariable(supplierName = "updateCompletionTime")
    @JsonIgnore
    private JobCompletionTime completionTime;
    private int processTimeSum = 0;

    public Job() {
    }

    public Job(int id, Machine[] allMachines) {
        this.id = id;
        this.allMachines = allMachines;
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

    public JobCompletionTime getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(JobCompletionTime completionTime) {
        this.completionTime = completionTime;
    }

    public int getProcessingTimeSum() {
        if (processTimeSum == 0) {
            for (var allMachine : allMachines) {
                processTimeSum += allMachine.getProcessTime(id);
            }
        }
        return processTimeSum;
    }

    public int getProcessingTime(int machineId) {
        return allMachines[machineId].getProcessTime(id);
    }

    @JsonIgnore
    @ShadowSources(value = { "previousJob.completionTime", "machine" })
    public JobCompletionTime updateCompletionTime() {
        if (machine == null) {
            return null;
        }
        var newCompletionTime = new JobCompletionTime(allMachines.length);
        // A machine can perform only one job at a time,
        // and a job can only start on one machine after finishing the process at the previous machine.
        // The completion time of this job in the first machine depends only on the previous job completion time.
        // It can only start after the previous job is completed.
        var previousMachineCompletionTime = newCompletionTime.setCompletionTime(0, getPreviousCompletionTime(0) + allMachines[0].getProcessTime(id));
        for (var i = 1; i < allMachines.length; i++) {
            // The job execution for the following machines relies on the completion time of either the previous job
            // or the previous machine,
            // depending on which is greater. 
            // That way, the job can only begin on the machine once it has completed on the previous machine
            // or after the prior job has finished.
            previousMachineCompletionTime = newCompletionTime.setCompletionTime(i,
                    Math.max(getPreviousCompletionTime(i), previousMachineCompletionTime) + allMachines[i].getProcessTime(id));
        }
        return newCompletionTime;
    }

    @JsonIgnore
    private int getPreviousCompletionTime(int machineId) {
        if (previousJob != null) {
            return previousJob.getCompletionTime(machineId);
        }
        return 0;
    }

    @JsonIgnore
    public int getCompletionTime(int machineId) {
        if (completionTime == null) {
            return 0;
        }
        return completionTime.getCompletionTime(machineId);
    }

    @JsonIgnore
    public int getCompletionTimeLastMachine() {
        if (completionTime == null) {
            return 0;
        }
        return completionTime.getCompletionTimeLastMachine();
    }

    @Override
    public String toString() {
        return "Job " + id;
    }
}
