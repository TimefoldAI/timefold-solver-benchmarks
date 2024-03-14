package ai.timefold.solver.benchmarks.examples.common.app;

import java.io.File;
import java.util.List;
import java.util.Set;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public abstract class ImportDirSolveAllTurtleTest<Solution_> extends SolveAllTurtleTest<Solution_> {

    private static <Solution_> AbstractSolutionImporter<Solution_>
            createSolutionImporter(CommonApp<Solution_> commonApp) {
        Set<AbstractSolutionImporter<Solution_>> importers = commonApp.createSolutionImporters();
        if (importers.size() != 1) {
            throw new IllegalStateException("The importers size (" + importers.size() + ") should be 1.");
        }
        return importers.stream()
                .findFirst()
                .orElseThrow();
    }

    @Override
    protected List<File> getSolutionFiles(CommonApp<Solution_> commonApp) {
        File importDataDir = commonApp.getImportDataDir();
        if (!importDataDir.exists()) {
            throw new IllegalStateException("The directory importDataDir (" + importDataDir.getAbsolutePath()
                    + ") does not exist.");
        } else {
            return getAllFilesRecursivelyAndSorted(importDataDir, createSolutionImporter(commonApp)::acceptInputFile);
        }
    }

    @Override
    protected ProblemFactory<Solution_> createProblemFactory(CommonApp<Solution_> commonApp) {
        AbstractSolutionImporter<Solution_> solutionImporter = createSolutionImporter(commonApp);
        return solutionImporter::readSolution;
    }
}
