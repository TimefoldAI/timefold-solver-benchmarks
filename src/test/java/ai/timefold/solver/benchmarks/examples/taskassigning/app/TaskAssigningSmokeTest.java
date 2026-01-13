package ai.timefold.solver.benchmarks.examples.taskassigning.app;

import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.examples.common.app.SolverSmokeTest;
import ai.timefold.solver.benchmarks.examples.taskassigning.domain.TaskAssigningSolution;
import ai.timefold.solver.core.api.score.buildin.bendablelong.BendableLongScore;

class TaskAssigningSmokeTest extends SolverSmokeTest<TaskAssigningSolution, BendableLongScore> {

    private static final String UNSOLVED_DATA_FILE = "data/taskassigning/unsolved/50tasks-5employees.json";

    @Override
    protected TaskAssigningApp createCommonApp() {
        return new TaskAssigningApp();
    }

    @Override
    protected Stream<TestData<BendableLongScore>> testData() {
        return Stream.of(
                TestData.of(UNSOLVED_DATA_FILE,
                        BendableLongScore.of(new long[] { 0 }, new long[] { 0, -3925, -6293940, -7772, -20463 }),
                        BendableLongScore.of(new long[] { 0 }, new long[] { 0, -3925, -6312519, -10049, -20937 })));
    }
}
