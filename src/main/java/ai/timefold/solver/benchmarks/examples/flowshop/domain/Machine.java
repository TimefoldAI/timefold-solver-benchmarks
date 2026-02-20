package ai.timefold.solver.benchmarks.examples.flowshop.domain;

import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Machine.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@PlanningEntity
public class Machine {

    @PlanningId
    private int id;
    private int[] processTime;
    @PlanningListVariable
    @JsonIdentityReference(alwaysAsId = true)
    private List<Job> jobs;

    public Machine() {
        this.jobs = new ArrayList<>();
    }

    public Machine(int id, int[] processTime) {
        this.id = id;
        this.processTime = processTime;
        this.jobs = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public int[] getProcessTime() {
        return processTime;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    @JsonIgnore
    public int getProcessTime(int jobId) {
        return processTime[jobId];
    }

    public int getMakespan() {
        if (jobs.isEmpty()) {
            return 0;
        }
        return jobs.getLast().getCompletionTimeLastMachine();
    }

    @Override
    public String toString() {
        return "Machine " + id;
    }
}
