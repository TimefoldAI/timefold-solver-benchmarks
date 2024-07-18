package ai.timefold.solver.benchmarks.examples.examination.persistence;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.jackson.api.domain.solution.AbstractConstraintWeightOverridesDeserializer;

public class ExaminationConstraintWeightOverrideDeserializer
        extends AbstractConstraintWeightOverridesDeserializer<HardSoftScore> {

    @Override
    protected HardSoftScore parseScore(String scoreString) {
        return HardSoftScore.parseScore(scoreString);
    }

}
