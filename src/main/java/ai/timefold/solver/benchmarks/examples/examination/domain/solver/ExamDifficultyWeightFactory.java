package ai.timefold.solver.benchmarks.examples.examination.domain.solver;

import java.util.Comparator;

import ai.timefold.solver.benchmarks.examples.examination.domain.Exam;
import ai.timefold.solver.benchmarks.examples.examination.domain.Examination;
import ai.timefold.solver.benchmarks.examples.examination.domain.LeadingExam;
import ai.timefold.solver.benchmarks.examples.examination.domain.PeriodPenalty;
import ai.timefold.solver.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;

public class ExamDifficultyWeightFactory implements SelectionSorterWeightFactory<Examination, Exam> {

    @Override
    public ExamDifficultyWeight createSorterWeight(Examination examination, Exam exam) {
        int studentSizeTotal = exam.getTopicStudentSize();
        int maximumDuration = exam.getTopicDuration();
        for (PeriodPenalty periodPenalty : examination.getPeriodPenaltyList()) {
            if (periodPenalty.getLeftTopic().equals(exam.getTopic())) {
                switch (periodPenalty.getPeriodPenaltyType()) {
                    case EXAM_COINCIDENCE:
                        studentSizeTotal += periodPenalty.getRightTopic().getStudentSize();
                        maximumDuration = Math.max(maximumDuration, periodPenalty.getRightTopic().getDuration());
                        break;
                    case EXCLUSION:
                        // Do nothing
                        break;
                    case AFTER:
                        // Do nothing
                        break;
                    default:
                        throw new IllegalStateException("The periodPenaltyType ("
                                + periodPenalty.getPeriodPenaltyType() + ") is not implemented.");
                }
            } else if (periodPenalty.getRightTopic().equals(exam.getTopic())) {
                switch (periodPenalty.getPeriodPenaltyType()) {
                    case EXAM_COINCIDENCE:
                        studentSizeTotal += periodPenalty.getLeftTopic().getStudentSize();
                        maximumDuration = Math.max(maximumDuration, periodPenalty.getLeftTopic().getDuration());
                        break;
                    case EXCLUSION:
                        // Do nothing
                        break;
                    case AFTER:
                        studentSizeTotal += periodPenalty.getLeftTopic().getStudentSize();
                        maximumDuration = Math.max(maximumDuration, periodPenalty.getLeftTopic().getDuration());
                        break;
                    default:
                        throw new IllegalStateException("The periodPenaltyType ("
                                + periodPenalty.getPeriodPenaltyType() + ") is not implemented.");
                }
            }
        }
        return new ExamDifficultyWeight(exam, studentSizeTotal, maximumDuration);
    }

    public static class ExamDifficultyWeight implements Comparable<ExamDifficultyWeight> {

        private static final Comparator<ExamDifficultyWeight> COMPARATOR = Comparator
                .comparingInt((ExamDifficultyWeight weight) -> weight.studentSizeTotal)
                .thenComparingInt(weight -> weight.maximumDuration)
                .thenComparing(weight -> weight.exam instanceof LeadingExam)
                .thenComparingLong(weight -> weight.exam.getId());

        private final Exam exam;
        private final int studentSizeTotal;
        private final int maximumDuration;

        public ExamDifficultyWeight(Exam exam, int studentSizeTotal, int maximumDuration) {
            this.exam = exam;
            this.studentSizeTotal = studentSizeTotal;
            this.maximumDuration = maximumDuration;
        }

        @Override
        public int compareTo(ExamDifficultyWeight other) {
            return COMPARATOR.compare(this, other);
        }

    }

}
