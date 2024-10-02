package ai.timefold.solver.benchmarks.micro.factorial.configuration;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import ai.timefold.solver.benchmarks.micro.factorial.Factor;

public class Experiment implements ExperimentWriter, Closeable {

    private final List<String> outputColumns;
    private final File outputFile;
    private final PrintWriter outputWriter;
    private final PrintWriter outputLogWriter;
    private final List<Observation> observationList = new ArrayList<>();
    private Long seed;
    private boolean persist = true;

    public Experiment(List<String> outputColumns) {
        this.outputColumns = outputColumns;
        var fileName = "result_%s".formatted(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss")));
        try {
            File outputLogFile = new File("/tmp/%s.log".formatted(fileName));
            this.outputLogWriter = new PrintWriter(outputLogFile);
            this.outputFile = new File("/tmp/%s.csv".formatted(fileName));
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
        log("Warm-up started, (%d) observations (%d) seconds.".formatted(size, timeInSeconds));
        persist = false;
        runObservationList(timeInSeconds, observationList.subList(0, size));
        persist = true;
        log("Warm-up finished");
    }

    public void run(long observationTimeInSeconds) {
        runObservationList(observationTimeInSeconds, observationList);
    }

    private void runObservationList(long observationTimeInSeconds, List<Observation> observations) {
        if (seed == null) {
            this.seed = System.nanoTime();
            setExperimentSeed(seed);
        }
        for (Observation observation : observations) {
            observation.set(new DummyConfiguration("experimentSeed", seed));
            observation.set(new SingleConfiguration("runTimeInSeconds", observationTimeInSeconds));
            observation.set(new SingleConfiguration("seed", System.nanoTime()));
            observation.run();
        }
        log("Result wrote to file %s".formatted(outputFile.getPath()));
    }

    public void generateObservations(List<Factor<?>> factorList) {
        generateObservations(new ArrayList<>(), factorList);
    }

    public void setExperimentSeed(long seed) {
        this.seed = seed;
        Collections.shuffle(observationList, new Random(seed));
        for (int i = 0; i < observationList.size(); i++) {
            observationList.get(i).setId(i + 1);
        }
    }

    private void generateObservations(List<AbstractConfiguration> currentConfiguration, List<Factor<?>> factorList) {
        if (factorList.size() > 1) {
            var factor = factorList.getFirst();
            factor.getLevelList().forEach(level -> {
                var newConfiguration = new ArrayList<AbstractConfiguration>(currentConfiguration);
                newConfiguration.add(new SingleConfiguration(factor.getName(), level.getValue()));
                generateObservations(newConfiguration, factorList.subList(1, factorList.size()));
            });
        } else {
            var factor = factorList.getFirst();
            factor.getLevelList().forEach(level -> {
                var newConfiguration = new ArrayList<AbstractConfiguration>(currentConfiguration);
                newConfiguration.add(new SingleConfiguration(factor.getName(), level.getValue()));
                addObservation(newConfiguration);
            });
        }
    }

    private void addObservation(List<AbstractConfiguration> configurationList) {
        observationList.add(new Observation(this, configurationList, outputColumns));
    }

    public void setConfiguration(AbstractConfiguration configuration) {
        observationList.forEach(o -> o.set(configuration));
    }

    @Override
    public void log(String message) {
        if (persist) {
            outputLogWriter.write("%s - %s%n".formatted(LocalDateTime.now().toString(), message));
            outputLogWriter.flush();
        }
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
        outputLogWriter.close();
        outputWriter.close();
    }
}
