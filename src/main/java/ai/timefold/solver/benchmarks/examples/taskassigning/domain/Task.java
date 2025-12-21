package ai.timefold.solver.benchmarks.examples.taskassigning.domain;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.CascadingUpdateShadowVariable;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class Task extends AbstractPersistable {

    private TaskType taskType;
    private int indexInTaskType;
    private Customer customer;
    private int minStartTime;
    private Priority priority;

    // Shadow variables
    @InverseRelationShadowVariable(sourceVariableName = "tasks")
    private Employee employee;
    @PreviousElementShadowVariable(sourceVariableName = "tasks")
    private Task previousTask;
    @CascadingUpdateShadowVariable(targetMethodName = "updateStartTime")
    private Integer startTime; // In minutes

    @JsonIgnore
    private Employee lastSeenEmployee;
    @JsonIgnore
    private int missingSkillCountCache = -1;

    public Task() {
    }

    public Task(long id, TaskType taskType, int indexInTaskType,
            Customer customer, int minStartTime, Priority priority) {
        super(id);
        this.taskType = taskType;
        this.indexInTaskType = indexInTaskType;
        this.customer = customer;
        this.minStartTime = minStartTime;
        this.priority = priority;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public int getIndexInTaskType() {
        return indexInTaskType;
    }

    public void setIndexInTaskType(int indexInTaskType) {
        this.indexInTaskType = indexInTaskType;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public int getMinStartTime() {
        return minStartTime;
    }

    public void setMinStartTime(int minStartTime) {
        this.minStartTime = minStartTime;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Task getPreviousTask() {
        return previousTask;
    }

    public void setPreviousTask(Task previousTask) {
        this.previousTask = previousTask;
    }

    public Integer getStartTime() {
        return startTime;
    }

    public void setStartTime(Integer startTime) {
        this.startTime = startTime;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @JsonIgnore
    public int getMissingSkillCount() {
        if (employee == null) {
            return 0;
        } else if (employee == lastSeenEmployee && missingSkillCountCache >= 0) {
            return missingSkillCountCache;
        }
        var count = 0;
        for (var skill : taskType.getRequiredSkillList()) {
            if (!employee.getSkillSet().contains(skill)) {
                count++;
            }
        }
        lastSeenEmployee = employee;
        missingSkillCountCache = count;
        return count;
    }

    /**
     * In minutes
     *
     * @return at least 1 minute
     */
    @JsonIgnore
    public int getDuration() {
        var affinity = getAffinity();
        return taskType.getBaseDuration() * affinity.getDurationMultiplier();
    }

    @JsonIgnore
    public Affinity getAffinity() {
        return (employee == null) ? Affinity.NONE : employee.getAffinity(customer);
    }

    @JsonIgnore
    public Integer getEndTime() {
        if (startTime == null) {
            return null;
        }
        return startTime + getDuration();
    }

    @JsonIgnore
    public String getCode() {
        return taskType + "-" + indexInTaskType;
    }

    @JsonIgnore
    public String getTitle() {
        return taskType.getTitle();
    }

    @SuppressWarnings("unused")
    protected void updateStartTime() {
        if (employee == null) {
            startTime = null;
        } else if (previousTask == null) {
            startTime = minStartTime;
        } else {
            var previousEndTime = previousTask.getEndTime();
            startTime = Math.max(previousEndTime, minStartTime);
        }
    }

    @Override
    public String toString() {
        return getCode();
    }

}
