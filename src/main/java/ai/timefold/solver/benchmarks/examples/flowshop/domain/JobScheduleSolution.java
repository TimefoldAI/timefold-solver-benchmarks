package ai.timefold.solver.benchmarks.examples.flowshop.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardSoftScore;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

@PlanningSolution
public class JobScheduleSolution {

    @PlanningEntityCollectionProperty
    @ValueRangeProvider
    private List<Job> jobs;
    @PlanningEntityProperty
    // We schedule the jobs for a single machine and replicate the sequence to the other machines (Permutation Flow-Shop Problem)
    @JsonIdentityReference(alwaysAsId = true)
    private Machine machine;
    // All machines of the flowshop
    private Machine[] allMachines;
    @PlanningScore
    private HardSoftScore score;

    public JobScheduleSolution() {
    }

    @JsonCreator
    public JobScheduleSolution(@JsonProperty("allMachines") Machine[] allMachines, @JsonProperty("jobs") List<Job> jobs) {
        this.allMachines = allMachines;
        this.machine = allMachines[0];
        this.jobs = jobs;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public Machine[] getAllMachines() {
        return allMachines;
    }

    public void setAllMachines(Machine[] allMachines) {
        this.allMachines = allMachines;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }
}
