package ai.timefold.solver.benchmarks.micro.factorial.planning;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import ai.timefold.solver.benchmarks.micro.factorial.configuration.AbstractConfiguration;
import ai.timefold.solver.benchmarks.micro.factorial.configuration.ExperimentConfiguration;
import ai.timefold.solver.benchmarks.micro.factorial.configuration.ObservationConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Experiment implements ExperimentWriter, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Experiment.class);
    private final List<String> outputColumns;
    private final File outputFile;
    private final PrintWriter outputWriter;
    private final List<Observation> observationList = new ArrayList<>();
    private Long seed;
    private boolean persist = true;

    public Experiment(List<String> outputColumns) {
        this.outputColumns = outputColumns;
        Path resultsDirectory = Path.of("results", "factorial",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss")));
        resultsDirectory.toFile().mkdirs();
        try {
            this.outputFile = new File(resultsDirectory.toFile(), "result.csv");
            this.outputWriter = new PrintWriter(this.outputFile);
            // Write the header
            this.outputWriter.write(String.join(";", outputColumns) + "\n");
            this.outputWriter.flush();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void warmup(long timeInSeconds, double samplePercentage) {
        var size = (int) (observationList.size() * samplePercentage);
        LOGGER.info("Warm-up started, observations ({}), seconds ({}).", size, timeInSeconds);
        persist = false;
        runObservationList(timeInSeconds, observationList.subList(0, size));
        persist = true;
        LOGGER.info("Warm-up finished");
    }

    public void run(long observationTimeInSeconds) {
        runObservationList(observationTimeInSeconds, observationList);
    }

    private void runObservationList(long observationTimeInSeconds, List<Observation> observations) {
        for (Observation observation : observations) {
            observation.set(new ExperimentConfiguration("experimentSeed", String.valueOf(seed)));
            observation.set(new ObservationConfiguration("runTimeInSeconds", String.valueOf(observationTimeInSeconds)));
            observation.set(new ObservationConfiguration("observationSeed", String.valueOf(System.nanoTime())));
            observation.run();
        }
        if (persist) {
            LOGGER.info("Result wrote to file {}", outputFile.getPath());
        }
    }

    public void generateObservations(List<Factor> factorList, int completeReplications, Long seed) {
        if (seed == null) {
            this.seed = System.nanoTime();
        }
        this.observationList.clear();
        for (int i = 0; i < completeReplications; i++) {
            generateObservations(new ArrayList<>(), factorList);
        }
        Collections.shuffle(observationList, new Random(this.seed));
        for (int i = 0; i < observationList.size(); i++) {
            observationList.get(i).setId(i + 1);
        }

    }

    private void generateObservations(List<AbstractConfiguration> currentConfiguration, List<Factor> factorList) {
        if (factorList.size() > 1) {
            factorList.getFirst().getLevelList().forEach(level -> {
                var newConfiguration = new ArrayList<>(currentConfiguration);
                newConfiguration.add(new ObservationConfiguration(level));
                generateObservations(newConfiguration, factorList.subList(1, factorList.size()));
            });
        } else {
            factorList.getFirst().getLevelList().forEach(level -> {
                var newConfiguration = new ArrayList<>(currentConfiguration);
                newConfiguration.add(new ObservationConfiguration(level));
                addObservation(newConfiguration);
            });
        }
    }

    private void addObservation(List<AbstractConfiguration> configurationList) {
        observationList.add(new Observation(this, configurationList, outputColumns));
    }

    public void addGlobalConfiguration(AbstractConfiguration configuration) {
        observationList.forEach(o -> o.set(configuration));
    }

    @Override
    public void saveResult(String result) {
        if (persist) {
            outputWriter.write(result);
            outputWriter.flush();
        }
    }

    @Override
    public void close() {
        outputWriter.close();
    }
}
