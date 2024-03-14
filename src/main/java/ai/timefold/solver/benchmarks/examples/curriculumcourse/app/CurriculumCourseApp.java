package ai.timefold.solver.benchmarks.examples.curriculumcourse.app;

import java.util.Collections;
import java.util.Set;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.CourseSchedule;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.persistence.CurriculumCourseImporter;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.persistence.CurriculumCourseSolutionFileIO;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

public class CurriculumCourseApp extends CommonApp<CourseSchedule> {

    public static final String SOLVER_CONFIG =
            "ai/timefold/solver/benchmarks/examples/curriculumcourse/curriculumCourseSolverConfig.xml";

    public static final String DATA_DIR_NAME = "curriculumcourse";

    public static void main(String[] args) {
        var solution = new CurriculumCourseApp().solve("comp07.json");
        System.out.println("Done: " + solution);
    }

    public CurriculumCourseApp() {
        super("Course timetabling",
                "Official competition name: ITC 2007 track3 - Curriculum course scheduling\n\n" +
                        "Assign lectures to periods and rooms.",
                SOLVER_CONFIG, DATA_DIR_NAME);
    }

    @Override
    public SolutionFileIO<CourseSchedule> createSolutionFileIO() {
        return new CurriculumCourseSolutionFileIO();
    }

    @Override
    protected Set<AbstractSolutionImporter<CourseSchedule>> createSolutionImporters() {
        return Collections.singleton(new CurriculumCourseImporter());
    }

}
