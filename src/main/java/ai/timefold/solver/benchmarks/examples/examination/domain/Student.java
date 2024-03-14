package ai.timefold.solver.benchmarks.examples.examination.domain;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;

/**
 * Not used during score calculation, so not inserted into the working memory.
 */
@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class Student extends AbstractPersistable {
    public Student() {
    }

    public Student(long id) {
        super(id);
    }
}
