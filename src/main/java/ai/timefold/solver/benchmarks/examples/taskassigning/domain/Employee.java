package ai.timefold.solver.benchmarks.examples.taskassigning.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.KeySerializer;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@PlanningEntity
@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class Employee extends AbstractPersistable {

    private String fullName;

    private Set<Skill> skillSet;
    private Map<Customer, Affinity> affinityMap;

    @PlanningListVariable(allowsUnassignedValues = true)
    private List<Task> tasks;

    public Employee() {
    }

    public Employee(long id, String fullName) {
        super(id);
        this.fullName = fullName;
        skillSet = new LinkedHashSet<>();
        affinityMap = new LinkedHashMap<>();
        tasks = new ArrayList<>();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Set<Skill> getSkillSet() {
        return skillSet;
    }

    public void setSkillSet(Set<Skill> skillSet) {
        this.skillSet = skillSet;
    }

    public Map<Customer, Affinity> getAffinityMap() {
        return affinityMap;
    }

    @JsonSerialize(keyUsing = KeySerializer.class)
    @JsonDeserialize(keyUsing = CustomerKeyDeserializer.class)
    public void
            setAffinityMap(Map<Customer, Affinity> affinityMap) {
        this.affinityMap = affinityMap;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    /**
     * @param customer never null
     * @return never null
     */
    @JsonIgnore
    public Affinity getAffinity(Customer customer) {
        Affinity affinity = affinityMap.get(customer);
        if (affinity == null) {
            affinity = Affinity.NONE;
        }
        return affinity;
    }

    @JsonIgnore
    public Integer getEndTime() {
        return tasks.isEmpty() ? 0 : tasks.get(tasks.size() - 1).getEndTime();
    }

    @Override
    public String toString() {
        return fullName;
    }

}
