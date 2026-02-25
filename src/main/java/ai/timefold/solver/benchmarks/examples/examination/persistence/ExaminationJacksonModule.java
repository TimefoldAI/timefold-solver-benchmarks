package ai.timefold.solver.benchmarks.examples.examination.persistence;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;

import tools.jackson.databind.module.SimpleModule;

public final class ExaminationJacksonModule extends SimpleModule {

    public ExaminationJacksonModule() {
        super("ExaminationJacksonModule");
        addDeserializer(ConstraintWeightOverrides.class, new ExaminationConstraintWeightOverrideDeserializer());
    }

}
