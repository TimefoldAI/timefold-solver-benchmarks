package ai.timefold.solver.benchmarks.examples.nurserostering.domain.contract;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.pattern.Pattern;

public class PatternContractLine extends AbstractPersistable {

    private Contract contract;
    private ai.timefold.solver.benchmarks.examples.nurserostering.domain.pattern.Pattern pattern;

    public PatternContractLine() {
    }

    public PatternContractLine(long id) {
        super(id);
    }

    public PatternContractLine(long id, Contract contract,
            ai.timefold.solver.benchmarks.examples.nurserostering.domain.pattern.Pattern pattern) {
        super(id);
        this.contract = contract;
        this.pattern = pattern;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public ai.timefold.solver.benchmarks.examples.nurserostering.domain.pattern.Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public String toString() {
        return contract + "-" + pattern;
    }

}
