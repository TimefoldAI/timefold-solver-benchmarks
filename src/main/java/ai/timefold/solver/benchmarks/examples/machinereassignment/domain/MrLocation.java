package ai.timefold.solver.benchmarks.examples.machinereassignment.domain;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;

@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class MrLocation extends AbstractPersistable {

    @SuppressWarnings("unused")
    MrLocation() {
    }

    public MrLocation(long id) {
        super(id);
    }
}
