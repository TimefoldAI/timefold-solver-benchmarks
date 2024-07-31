package ai.timefold.solver.benchmarks.examples.nurserostering.app;

import java.util.Collections;
import java.util.Set;

import ai.timefold.solver.benchmark.api.PlannerBenchmarkFactory;
import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.NurseRoster;
import ai.timefold.solver.benchmarks.examples.nurserostering.persistence.NurseRosterSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.nurserostering.persistence.NurseRosteringImporter;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

public class NurseRosteringApp extends CommonApp<NurseRoster> {

    public static final String SOLVER_CONFIG =
            "ai/timefold/solver/benchmarks/examples/nurserostering/nurseRosteringBenchmarkConfigLong.xml.ftl";

    public static final String DATA_DIR_NAME = "nurserostering";

    public static void main(String[] args) {
        var bench = PlannerBenchmarkFactory.createFromFreemarkerXmlResource("ai/timefold/solver/benchmarks/examples/nurserostering/nurseRosteringBenchmarkConfigSprint.xml.ftl")
                .buildPlannerBenchmark();
        bench.benchmark();

        bench = PlannerBenchmarkFactory.createFromFreemarkerXmlResource("ai/timefold/solver/benchmarks/examples/nurserostering/nurseRosteringBenchmarkConfigLong.xml.ftl")
                .buildPlannerBenchmark();
        bench.benchmark();
    }

    public NurseRosteringApp() {
        super("Nurse rostering",
                "Official competition name: INRC2010 - Nurse rostering\n\n" +
                        "Assign shifts to nurses.",
                SOLVER_CONFIG, DATA_DIR_NAME);
    }

    @Override
    public SolutionFileIO<NurseRoster> createSolutionFileIO() {
        return new NurseRosterSolutionFileIO();
    }

    @Override
    protected Set<AbstractSolutionImporter<NurseRoster>> createSolutionImporters() {
        return Collections.singleton(new NurseRosteringImporter());
    }

}
