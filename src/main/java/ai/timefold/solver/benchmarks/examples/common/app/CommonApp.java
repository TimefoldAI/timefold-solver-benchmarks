package ai.timefold.solver.benchmarks.examples.common.app;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.solver.SolverConfigOverride;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public abstract class CommonApp<Solution_> extends LoggingMain {

    /**
     * The path to the data directory, preferably with unix slashes for portability.
     * For example: -D{@value #DATA_DIR_SYSTEM_PROPERTY}=sources/data/
     */
    public static final String DATA_DIR_SYSTEM_PROPERTY = "ai.timefold.solver.examples.dataDir";

    public static File determineDataDir(String dataDirName) {
        String dataDirPath = System.getProperty(DATA_DIR_SYSTEM_PROPERTY, "data/");
        File dataDir = new File(dataDirPath, dataDirName);
        if (!dataDir.exists()) {
            throw new IllegalStateException("The directory dataDir (" + dataDir.getAbsolutePath()
                    + ") does not exist.\n" +
                    " Either the working directory should be set to the directory that contains the data directory" +
                    " (which is not the data directory itself), or the system property "
                    + DATA_DIR_SYSTEM_PROPERTY + " should be set properly.\n" +
                    " The data directory is different in a git clone (timefold/timefold-solver-examples/data)" +
                    " and in a release zip (examples/sources/data).\n" +
                    " In an IDE (IntelliJ, Eclipse, VSCode), open the \"Run configuration\""
                    + " to change \"Working directory\" (or add the system property in \"VM options\").");
        }
        return dataDir;
    }

    public static String getBaseFileName(File file) {
        return getBaseFileName(file.getName());
    }

    public static String getBaseFileName(String name) {
        int indexOfLastDot = name.lastIndexOf('.');
        if (indexOfLastDot > 0) {
            return name.substring(0, indexOfLastDot);
        } else {
            return name;
        }
    }

    protected final String name;
    protected final String description;
    protected final String solverConfigResource;
    protected final String dataDirName;
    protected final File importDataDir;
    protected final File unsolvedDataDir;
    protected final File solvedDataDir;

    protected CommonApp(String name, String description, String solverConfigResource, String dataDirName) {
        this.name = name;
        this.description = description;
        this.solverConfigResource = solverConfigResource;
        this.dataDirName = dataDirName;
        this.importDataDir = new File(determineDataDir(dataDirName), "import");
        if (!createSolutionImporters().isEmpty() && !importDataDir.exists()) {
            throw new IllegalStateException("The directory importDataDir (" + importDataDir.getAbsolutePath()
                    + ") does not exist.");
        }
        this.unsolvedDataDir = new File(determineDataDir(dataDirName), "unsolved");
        if (!unsolvedDataDir.exists()) {
            throw new IllegalStateException("The directory unsolvedDataDir (" + unsolvedDataDir.getAbsolutePath()
                    + ") does not exist.");
        }
        this.solvedDataDir = new File(determineDataDir(dataDirName), "solved");
        if (!solvedDataDir.exists() && !solvedDataDir.mkdir()) {
            throw new IllegalStateException("The directory solvedDataDir (" + solvedDataDir.getAbsolutePath()
                    + ") does not exist and could not be created.");
        }

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSolverConfigResource() {
        return solverConfigResource;
    }

    public String getDataDirName() {
        return dataDirName;
    }

    public File getImportDataDir() {
        return importDataDir;
    }

    public File getUnsolvedDataDir() {
        return unsolvedDataDir;
    }

    public File getSolvedDataDir() {
        return solvedDataDir;
    }

    /**
     * Used for the unsolved and solved directories,
     * not for the import and output directories, in the data directory.
     *
     * @return never null
     */
    public abstract SolutionFileIO<Solution_> createSolutionFileIO();

    protected Set<AbstractSolutionImporter<Solution_>> createSolutionImporters() {
        return Collections.emptySet();
    }

    public final Solution_ solve(String datasetName) {
        return solve(datasetName, 1L);
    }

    public final Solution_ solve(String datasetName, long minutesSpentLimit) {
        var solutionFileIo = createSolutionFileIO();
        var solution = solutionFileIo.read(Path.of("data", dataDirName, "unsolved", datasetName).toFile().getAbsoluteFile());
        var solverFactory = SolverFactory.<Solution_> createFromXmlResource(solverConfigResource);
        var solver = solverFactory.buildSolver(new SolverConfigOverride<Solution_>()
                .withTerminationConfig(new TerminationConfig()
                        .withMinutesSpentLimit(minutesSpentLimit)));
        return solver.solve(solution);
    }

}
