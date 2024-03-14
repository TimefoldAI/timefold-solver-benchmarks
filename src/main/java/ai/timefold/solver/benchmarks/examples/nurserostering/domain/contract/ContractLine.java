package ai.timefold.solver.benchmarks.examples.nurserostering.domain.contract;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BooleanContractLine.class, name = "boolean"),
        @JsonSubTypes.Type(value = MinMaxContractLine.class, name = "minMax"),
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public abstract class ContractLine extends AbstractPersistable {

    private ai.timefold.solver.benchmarks.examples.nurserostering.domain.contract.Contract contract;
    private ai.timefold.solver.benchmarks.examples.nurserostering.domain.contract.ContractLineType contractLineType;

    protected ContractLine() {
    }

    protected ContractLine(long id, ai.timefold.solver.benchmarks.examples.nurserostering.domain.contract.Contract contract,
            ai.timefold.solver.benchmarks.examples.nurserostering.domain.contract.ContractLineType contractLineType) {
        super(id);
        this.contract = contract;
        this.contractLineType = contractLineType;
    }

    public ai.timefold.solver.benchmarks.examples.nurserostering.domain.contract.Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public ai.timefold.solver.benchmarks.examples.nurserostering.domain.contract.ContractLineType getContractLineType() {
        return contractLineType;
    }

    public void setContractLineType(ContractLineType contractLineType) {
        this.contractLineType = contractLineType;
    }

    public abstract boolean isEnabled();

    @Override
    public String toString() {
        return contract + "-" + contractLineType;
    }
}
