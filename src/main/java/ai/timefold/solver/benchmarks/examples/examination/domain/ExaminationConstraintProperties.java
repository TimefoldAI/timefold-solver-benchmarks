package ai.timefold.solver.benchmarks.examples.examination.domain;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;

public class ExaminationConstraintProperties extends AbstractPersistable {

    private int periodSpreadLength;
    private int frontLoadLargeTopicSize;
    private int frontLoadLastPeriodSize;

    public ExaminationConstraintProperties() {
        this(0);
    }

    public ExaminationConstraintProperties(long id) {
        super(id);
    }

    // ************************************************************************
    // Constraint weight methods
    // ************************************************************************

    public int getPeriodSpreadLength() {
        return periodSpreadLength;
    }

    public void setPeriodSpreadLength(int periodSpreadLength) {
        this.periodSpreadLength = periodSpreadLength;
    }

    public int getFrontLoadLargeTopicSize() {
        return frontLoadLargeTopicSize;
    }

    public void setFrontLoadLargeTopicSize(int frontLoadLargeTopicSize) {
        this.frontLoadLargeTopicSize = frontLoadLargeTopicSize;
    }

    public int getFrontLoadLastPeriodSize() {
        return frontLoadLastPeriodSize;
    }

    public void setFrontLoadLastPeriodSize(int frontLoadLastPeriodSize) {
        this.frontLoadLastPeriodSize = frontLoadLastPeriodSize;
    }
}
