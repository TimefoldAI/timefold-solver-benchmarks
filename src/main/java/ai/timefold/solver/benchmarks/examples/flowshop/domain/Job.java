package ai.timefold.solver.benchmarks.examples.flowshop.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.IndexShadowVariable;
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
    @IndexShadowVariable(sourceVariableName = "jobs")
    @JsonIgnore
    private Integer index;
    @PreviousElementShadowVariable(sourceVariableName = "jobs")
    @JsonIgnore
    private Job previousJob;
    @ShadowVariable(supplierName = "updateMakespan")
    @JsonIgnore
    private JobMakespan makespan;

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

    public JobMakespan getMakespan() {
        return makespan;
    }

    public void setMakespan(JobMakespan makespan) {
        this.makespan = makespan;
    }

    @JsonIgnore
    @ShadowSources(value = { "previousJob.makespan", "index" })
    public JobMakespan updateMakespan() {
        if (index == null) {
            return null;
        }
        var newMakespan = new JobMakespan(allMachines.length);
        // A machine can perform only one job at a time,
        // and a job can only start on one machine after finishing the process at the previous machine.
        // The makespan of this job in the first machine depends only on the previous job makespan.
        // It can only start after the previous job is completed.
        var newPreviousMakespan = newMakespan.setMakespan(0, getPreviousMakespan(0) + allMachines[0].getProcessTime(id));
        for (var i = 1; i < allMachines.length; i++) {
            // The job execution for the following machines relies on the makespan of either the previous job
            // or the previous machine,
            // depending on which is greater. 
            // That way, the job can only begin on the machine once it has completed on the previous machine
            // or after the prior job has finished.
            newPreviousMakespan = newMakespan.setMakespan(i,
                    Math.max(getPreviousMakespan(i), newPreviousMakespan) + allMachines[i].getProcessTime(id));
        }
        return newMakespan;
    }

    @JsonIgnore
    private int getPreviousMakespan(int machineId) {
        if (previousJob != null) {
            return previousJob.getMakespan(machineId);
        }
        return 0;
    }

    @JsonIgnore
    public int getMakespan(int machineId) {
        if (makespan == null) {
            return 0;
        }
        return makespan.getMakespan(machineId);
    }

    @JsonIgnore
    public int getLastMachineMakespan() {
        if (makespan == null) {
            return 0;
        }
        // The makespan is given by the makespan of the last machine
        return makespan.getLastMachineMakespan();
    }

    @Override
    public String toString() {
        return "Job " + (id + 1);
    }
}
