package ai.timefold.solver.benchmarks.examples.examination.app;

import java.util.Collections;
import java.util.Set;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.app.CurriculumCourseApp;
import ai.timefold.solver.benchmarks.examples.examination.domain.Examination;
import ai.timefold.solver.benchmarks.examples.examination.persistence.ExaminationImporter;
import ai.timefold.solver.benchmarks.examples.examination.persistence.ExaminationSolutionFileIO;
import ai.timefold.solver.core.api.domain.solution.SolutionFileIO;

/**
 * Examination is super optimized and a bit complex.
 * {@link CurriculumCourseApp} is arguably a better example to learn from.
 */
public class ExaminationApp extends CommonApp<Examination> {

    public static final String SOLVER_CONFIG = "ai/timefold/solver/benchmarks/examples/examination/examinationSolverConfig.xml";

    public static final String DATA_DIR_NAME = "examination";

    public static void main(String[] args) {
        var solution = new ExaminationApp().solve("exam_comp_set8.json");
        System.out.println("Done: " + solution);
    }

    public ExaminationApp() {
        super("Exam timetabling",
                "Official competition name: ITC 2007 track1 - Examination timetabling\n\n" +
                        "Assign exams to timeslots and rooms.",
                SOLVER_CONFIG, DATA_DIR_NAME);
    }

    @Override
    public SolutionFileIO<Examination> createSolutionFileIO() {
        return new ExaminationSolutionFileIO();
    }

    @Override
    protected Set<AbstractSolutionImporter<Examination>> createSolutionImporters() {
        return Collections.singleton(new ExaminationImporter());
    }

}
