package ai.timefold.solver.benchmarks.examples.examination.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;
import ai.timefold.solver.core.preview.api.domain.variable.declarative.ShadowSources;

@PlanningEntity
public class FollowingExam extends Exam {

    protected LeadingExam leadingExam;

    // Shadow variables
    protected Period period;

    public LeadingExam getLeadingExam() {
        return leadingExam;
    }

    public void setLeadingExam(LeadingExam leadingExam) {
        this.leadingExam = leadingExam;
    }

    @Override
    @ShadowVariable(supplierName = "periodSupplier")
    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    @ShadowSources("leadingExam.period")
    public Period periodSupplier() {
        return leadingExam.getPeriod();
    }

    // ************************************************************************
    // With methods
    // ************************************************************************

    public FollowingExam withId(long id) {
        this.setId(id);
        return this;
    }

    public FollowingExam withTopic(Topic topic) {
        this.setTopic(topic);
        return this;
    }

    public FollowingExam withRoom(Room room) {
        this.setRoom(room);
        return this;
    }

    public FollowingExam withPeriod(Period period) {
        this.setPeriod(period);
        return this;
    }

    public FollowingExam withLeadingExam(LeadingExam leadingExam) {
        this.setLeadingExam(leadingExam);
        return this;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
