package ai.timefold.solver.benchmarks.competitive.flowshop;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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

    private static void convertFromVFRToTaillardLayout() {
        convertFromVFRToTaillardLayout("data/flowshop/import/vfr-small2015-input/solutions.csv",
                "data/flowshop/import/vfr-small2015-input",
                "data/flowshop/import/vfr-small2015");
        convertFromVFRToTaillardLayout("data/flowshop/import/vfr-large2015-input/solutions.csv",
                "data/flowshop/import/vfr-large2015-input",
                "data/flowshop/import/vfr-large2015");
    }

    /**
     * The method creates new instances and converts the format used by VFR to that used by Taillard.
     */
    private static void convertFromVFRToTaillardLayout(String solutionsPath, String sourcePath, String targetPath) {
        var solutionFile = new File(solutionsPath);

        var sourceDir = new File(sourcePath);
        var allFiles = Arrays.asList(sourceDir.listFiles());
        List<File> sourceFiles = new ArrayList<>(allFiles.stream().filter(f -> f.getAbsolutePath().endsWith("txt")).toList());

        var targetDir = new File(targetPath);
        if (targetDir.exists()) {
            var targetFiles = new ArrayList<>(Arrays.asList(targetDir.listFiles()));
            targetFiles.forEach(File::delete);
        } else {
            targetDir.mkdirs();
        }

        try (var reader = new BufferedReader(new FileReader(solutionFile))) {
            reader.lines().forEach(line -> {
                var lineArr = line.split(",");
                var instance = lineArr[0];
                var upperBound = Integer.parseInt(lineArr[1]);
                var sourceFile = sourceFiles.stream().filter(f -> f.getName().equals(instance + ".txt")).findFirst().get();
                var targetFile = new File(targetPath, instance + ".txt");
                try (var sourceReader = new BufferedReader(new FileReader(sourceFile));
                        var writer = new BufferedWriter(new FileWriter(targetFile))) {
                    var allSourceLines = sourceReader.lines().toList();
                    var firstLine = allSourceLines.get(0);
                    try (var firstLineScanner = new Scanner(firstLine)) {
                        var jobs = firstLineScanner.nextInt();
                        var machines = firstLineScanner.nextInt();
                        var processTime = new int[machines][jobs];
                        var updatedFirstLine = "%s  %d  %d".formatted(firstLine, 0, upperBound);
                        writer.write(updatedFirstLine);
                        var jobIdx = 0;
                        for (var i = 1; i < allSourceLines.size(); i++) {
                            try (var lineScanner = new Scanner(allSourceLines.get(i))) {
                                var machineIdx = 0;
                                while (lineScanner.hasNext()) {
                                    // Ignore the column
                                    lineScanner.next();
                                    // Get process time from job jobIdx of the machine machineIdx
                                    processTime[machineIdx++][jobIdx] = lineScanner.nextInt();
                                }
                            }
                            jobIdx++;
                        }
                        for (var i = 0; i < machines; i++) {
                            var timeList = new ArrayList<>(jobs);
                            for (var j = 0; j < jobs; j++) {
                                timeList.add(processTime[i][j]);
                            }
                            var newLine = timeList.stream().map(String::valueOf).collect(Collectors.joining(" "));
                            writer.newLine();
                            writer.write(newLine);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateDatasetEnum() {
        var enumList = new ArrayList<String>();
        enumList.add("// 120 instances - Taillard93");
        enumList.addAll(generateDatasetEnum("data/flowshop/import/taillard93", "taillard93"));
        enumList.add("// 240 instances - VFR-small2015");
        enumList.addAll(generateDatasetEnum("data/flowshop/import/vfr-small2015", "vfr-small2015"));
        enumList.add("// 240 instances - VFR-large2015");
        enumList.addAll(generateDatasetEnum("data/flowshop/import/vfr-large2015", "vfr-large2015"));
        System.out.println(enumList.stream().collect(Collectors.joining("\n")));
    }

    private static List<String> generateDatasetEnum(String directoryPath, String module) {
        var directory = new File(directoryPath);
        var files = new ArrayList<>(Arrays.asList(directory.listFiles()));
        Collections.sort(files, Comparator.comparing(File::getName));
        var enumList = new ArrayList<String>();
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
                        enumList.add("%s(\"%s\", \"%s\", %d, %d),".formatted(name, module, file.getName(), jobs, upperBound));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return enumList;
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
