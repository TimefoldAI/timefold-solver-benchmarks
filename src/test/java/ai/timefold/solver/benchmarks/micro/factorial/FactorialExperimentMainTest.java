package ai.timefold.solver.benchmarks.micro.factorial;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.benchmarks.micro.factorial.planning.Experiment;

import org.junit.jupiter.api.Test;

class FactorialExperimentMainTest {

    @Test
    void testDeserialization() {
        FactorialConfiguration configuration = FactorialExperimentMain
                .readConfiguration(this.getClass().getResource("/factorial-benchmark.json").getFile());
        assertThat(configuration).isNotNull();
        assertThat(configuration.getOutputColumns()).hasSize(16);
        assertThat(configuration.getFactors()).hasSize(3);
        assertThat(configuration.getGlobalConfigurations()).hasSize(4);
        assertThat(configuration.getExperimentSeed()).isNull();
        assertThat(configuration.getCompleteReplications()).isOne();
        assertThat(configuration.getWarmupTimeInSeconds()).isEqualTo(30);
        assertThat(configuration.getWarmupRatio()).isEqualTo(0.3);
        assertThat(configuration.getObservationTimeInSeconds()).isZero();
    }

    @Test
    void testConfiguration() {
        FactorialConfiguration configuration = FactorialExperimentMain
                .readConfiguration(Objects.requireNonNull(this.getClass().getResource("/factorial-benchmark.json")).getFile());
        try (Experiment experiment = new Experiment(configuration.getOutputColumns())) {
            experiment.generateObservations(configuration.getFactors(), configuration.getCompleteReplications(),
                    configuration.getExperimentSeed());
            configuration.getGlobalConfigurations().forEach(experiment::addGlobalConfiguration);
            experiment.run(configuration.getObservationTimeInSeconds());
            var expectedObservations = new ArrayList<ExpectedObservation>();
            for (var approach : List.of("forager", "acceptor")) {
                for (var type : List.of("cloudbalancing", "conferencescheduling", "tsp", "vehiclerouting")) {
                    for (var size : List.of("small", "medium", "large")) {
                        var expectedFactorLevelList = new ArrayList<ExpectedFactorLevel>();
                        expectedFactorLevelList.add(new ExpectedFactorLevel("approach", approach));
                        expectedFactorLevelList.add(new ExpectedFactorLevel("type", type));
                        expectedFactorLevelList.add(new ExpectedFactorLevel("size", size));
                        if (approach.equals("forager")) {
                            expectedFactorLevelList.add(new ExpectedFactorLevel("selectedCountLimitRatio", "100"));
                            expectedFactorLevelList.add(new ExpectedFactorLevel("moveCountLimitPercentage", null));
                            expectedFactorLevelList.add(new ExpectedFactorLevel("lateAcceptanceReconfigurationSize", null));
                        } else {
                            expectedFactorLevelList.add(new ExpectedFactorLevel("selectedCountLimitRatio", null));
                            expectedFactorLevelList.add(new ExpectedFactorLevel("moveCountLimitPercentage", "100"));
                            expectedFactorLevelList.add(new ExpectedFactorLevel("lateAcceptanceReconfigurationSize", "1"));
                        }
                        switch (type) {
                            case "cloudbalancing": {
                                switch (size) {
                                    case "small": {
                                        expectedFactorLevelList.add(new ExpectedFactorLevel("datasetName",
                                                "4computers-12processes.json"));
                                        break;
                                    }
                                    case "medium": {
                                        expectedFactorLevelList.add(new ExpectedFactorLevel("datasetName",
                                                "100computers-300processes.json"));
                                        break;
                                    }
                                    case "large": {
                                        expectedFactorLevelList.add(new ExpectedFactorLevel("datasetName",
                                                "1600computers-4800processes.json"));
                                        break;
                                    }
                                    default:
                                        throw new IllegalStateException("Unexpected size: %s".formatted(size));
                                }
                                break;
                            }
                            case "conferencescheduling": {
                                switch (size) {
                                    case "small": {
                                        expectedFactorLevelList.add(new ExpectedFactorLevel("datasetName",
                                                "18talks-6timeslots-5rooms.xlsx"));
                                        break;
                                    }
                                    case "medium": {
                                        expectedFactorLevelList.add(new ExpectedFactorLevel("datasetName",
                                                "72talks-12timeslots-10rooms.xlsx"));
                                        break;
                                    }
                                    case "large": {
                                        expectedFactorLevelList.add(new ExpectedFactorLevel("datasetName",
                                                "216talks-18timeslots-20rooms.xlsx"));
                                        break;
                                    }
                                    default:
                                        throw new IllegalStateException("Unexpected size: %s".formatted(size));
                                }
                                break;
                            }
                            case "tsp": {
                                switch (size) {
                                    case "small": {
                                        expectedFactorLevelList.add(new ExpectedFactorLevel("datasetName",
                                                "belgium-n50.json"));
                                        break;
                                    }
                                    case "medium": {
                                        expectedFactorLevelList.add(new ExpectedFactorLevel("datasetName",
                                                "belgium-n100.json"));
                                        break;
                                    }
                                    case "large": {
                                        expectedFactorLevelList.add(new ExpectedFactorLevel("datasetName",
                                                "belgium-n1000.json"));
                                        break;
                                    }
                                    default:
                                        throw new IllegalStateException("Unexpected size: %s".formatted(size));
                                }
                                break;
                            }
                            case "vehiclerouting": {
                                switch (size) {
                                    case "small": {
                                        expectedFactorLevelList.add(new ExpectedFactorLevel("datasetName",
                                                "cvrptw-25customers.json"));
                                        break;
                                    }
                                    case "medium": {
                                        expectedFactorLevelList.add(new ExpectedFactorLevel("datasetName",
                                                "cvrptw-100customers-A.json"));
                                        break;
                                    }
                                    case "large": {
                                        expectedFactorLevelList.add(new ExpectedFactorLevel("datasetName",
                                                "cvrptw-400customers.json"));
                                        break;
                                    }
                                    default:
                                        throw new IllegalStateException("Unexpected size: %s".formatted(size));
                                }
                                break;
                            }
                            default:
                                throw new IllegalStateException("Unexpected type: %s".formatted(size));
                        }

                        expectedObservations.add(new ExpectedObservation(expectedFactorLevelList));
                    }
                }
            }
            var observations = experiment.getObservationList();
            for (var expectedObservation : expectedObservations) {
                var test =
                        observations.stream().anyMatch(o -> expectedObservation.expectedConfigurations.stream().allMatch(c -> {
                            var f = o.getValue(c.factor());
                            return Objects.equals(f, c.level());
                        }));
                assertThat(test).isTrue();
            }

        }
    }

    private record ExpectedObservation(List<ExpectedFactorLevel> expectedConfigurations) {

    }

    private record ExpectedFactorLevel(String factor, String level) {
    }
}
