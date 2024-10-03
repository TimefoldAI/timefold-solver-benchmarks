package ai.timefold.solver.benchmarks.micro.factorial;

import java.io.IOException;
import java.nio.file.Path;

import ai.timefold.solver.benchmarks.micro.factorial.planning.Experiment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FactorialExperimentMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(FactorialExperimentMain.class);

    private FactorialExperimentMain() {
    }

    private static FactorialConfiguration readConfiguration(String configurationFileName) {
        var configPath = configurationFileName != null ? Path.of(configurationFileName).toAbsolutePath()
                : Path.of("factorial-benchmark.json").toAbsolutePath();
        if (configPath.toFile().exists()) {
            LOGGER.info("Using benchmark configuration file: {}.", configPath);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(configPath.toFile(), FactorialConfiguration.class);
            } catch (IOException e) {
                throw new IllegalStateException("Failed reading benchmark configuration: " + configPath, e);
            }
        } else {
            throw new IllegalStateException(
                    "No benchmark configuration file found. Maybe add the file factorial-benchmark.json to the project folder.");
        }
    }

    public static void main(String[] args) {
        FactorialConfiguration configuration = readConfiguration(args.length > 0 ? args[0] : null);
        configuration.validate();
        try (Experiment experiment = new Experiment(configuration.getOutputColumns())) {
            experiment.generateObservations(configuration.getFactors(), configuration.getCompleteReplications(),
                    configuration.getExperimentSeed());
            if (configuration.getGlobalConfigurations() != null) {
                configuration.getGlobalConfigurations().forEach(experiment::addGlobalConfiguration);
            }
            experiment.warmup(configuration.getWarmupTimeInSeconds(), configuration.getWarmupRatio());
            experiment.run(configuration.getObservationTimeInSeconds());
        }
    }
}
