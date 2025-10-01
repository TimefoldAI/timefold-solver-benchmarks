package ai.timefold.solver.benchmarks.competitive.flowshop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import ai.timefold.solver.benchmarks.competitive.AbstractCompetitiveBenchmark;
import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.benchmarks.examples.flowshop.domain.JobScheduleSolution;
import ai.timefold.solver.benchmarks.examples.flowshop.persistence.TaillardImporter;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

public class Main extends
        AbstractCompetitiveBenchmark<FlowShopDataset, FlowShopConfiguration, JobScheduleSolution, HardSoftLongScore> {

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        var benchmark = new Main();
        benchmark.run(FlowShopConfiguration.COMMUNITY_EDITION, FlowShopConfiguration.ENTERPRISE_EDITION,
                FlowShopDataset.values());
    }

    private static void generateDatasetEnum() {
        var directoryPath = "data/flowshop/import/taillard93";
        var directory = new File(directoryPath);
        var files = new ArrayList<>(Arrays.asList(directory.listFiles()));
        Collections.sort(files, Comparator.comparing(File::getName));
        for (var file : files) {
            if (file.isFile()) { // Check if it's a regular file, not a directory
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    var line = reader.readLine();
                    try (var scanner = new Scanner(line)) {
                        // Number of jobs
                        var jobs = scanner.nextInt();
                        // Number of machines
                        scanner.next();
                        // Seed
                        scanner.next();
                        // Upper bound
                        var upperBound = scanner.nextInt();
                        String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
                        System.out.printf("%s(\"taillard93\", \"%s\", %d, %d),%n", name, file.getName(), jobs, upperBound);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected String getLibraryName() {
        return "FlowShop";
    }

    @Override
    protected HardSoftLongScore extractScore(JobScheduleSolution solution) {
        return solution.getScore();
    }

    @Override
    protected BigDecimal extractResult(FlowShopDataset dataset, HardSoftLongScore score) {
        return BigDecimal.valueOf(-score.softScore());
    }

    @Override
    protected int countValues(JobScheduleSolution solution) {
        return solution.getJobs().size();
    }

    @Override
    protected int countEntities(JobScheduleSolution solution) {
        return solution.getAllMachines().length;
    }

    @Override
    protected AbstractSolutionImporter<JobScheduleSolution> createImporter() {
        return new TaillardImporter();
    }
}
