package ai.timefold.solver.benchmarks.examples.examination.domain.solver;

import java.util.Comparator;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.examination.domain.Exam;
import ai.timefold.solver.benchmarks.examples.examination.domain.Examination;
import ai.timefold.solver.benchmarks.examples.examination.domain.LeadingExam;
import ai.timefold.solver.core.api.domain.common.ComparatorFactory;

public class ExamComparatorFactory implements ComparatorFactory<Examination, Exam> {

    @Override
    public Comparator<Exam> createComparator(Examination examination) {
        return Comparator.<Exam> comparingInt(exam -> exam.getStudentSizeTotal(examination))
                .thenComparingInt(exam -> exam.getMaximumDuration(examination))
                .thenComparing(LeadingExam.class::isInstance)
                .thenComparingLong(AbstractPersistable::getId);
    }
}
