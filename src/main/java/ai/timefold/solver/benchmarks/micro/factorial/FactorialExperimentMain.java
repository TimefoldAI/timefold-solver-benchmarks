package ai.timefold.solver.benchmarks.micro.factorial;

import java.util.List;

import ai.timefold.solver.benchmarks.micro.factorial.configuration.DummyConfiguration;
import ai.timefold.solver.benchmarks.micro.factorial.configuration.Experiment;

public class FactorialExperimentMain {

    private FactorialExperimentMain() {
    }

    public static void foragerParamCalibration() {
        var outputColumns = List.of(
                "type",
                "datasetName",
                "runTimeInSeconds",
                "experimentSeed",
                "seed",
                "selectedCountLimitRatio",
                "score",
                "scoreSpeed",
                "scoreCount",
                "scoreConverted",
                "scoreLogarithmic",
                "moveSpeed",
                "moveCount",
                "timeSpent");
        try (Experiment experiment = new Experiment(outputColumns)) {
            // Forager
            var selectedCountLimitRatioFactor = new Factor<>("selectedCountLimitRatio",
                    List.of(new Level<>(0.0), new Level<>(1.0), new Level<>(5.0), new Level<>(10.0),
                            new Level<>(50.0), new Level<>(100.0)));
            // 2 complete replications
            for (int i = 0; i < 2; i++) {
                experiment.generateObservations(List.of(selectedCountLimitRatioFactor));
            }
            experiment.setConfiguration(new DummyConfiguration("type", "vehiclerouting"));
            experiment.setConfiguration(new DummyConfiguration("datasetName", "cvrptw-25customers.json"));
            experiment.warmup(30L, 0.3);
            experiment.run(30L * 60L);
        }
    }

    public static void lateAcceptanceParamCalibration() {
        var outputColumns = List.of(
                "type",
                "datasetName",
                "runTimeInSeconds",
                "experimentSeed",
                "seed",
                "moveCountLimitPercentage",
                "lateAcceptanceReconfigurationSize",
                "score",
                "scoreSpeed",
                "scoreCount",
                "scoreConverted",
                "scoreLogarithmic",
                "moveSpeed",
                "moveCount",
                "timeSpent");
        try (Experiment experiment = new Experiment(outputColumns)) {
            // Late Acceptance
            var moveCountLimitPercentageFactor = new Factor<>("moveCountLimitPercentage",
                    List.of(new Level<>(0.0), new Level<>(1.0), new Level<>(5.0), new Level<>(10.0),
                            new Level<>(50.0), new Level<>(100.0)));
            var lateAcceptanceReconfigurationSizeFactor = new Factor<>("lateAcceptanceReconfigurationSize",
                    List.of(new Level<>(0L), new Level<>(1L), new Level<>(5L), new Level<>(10L),
                            new Level<>(50L), new Level<>(100L), new Level<>(200L), new Level<>(400L)));
            // 2 complete replications
            for (int i = 0; i < 2; i++) {
                experiment.generateObservations(List.of(moveCountLimitPercentageFactor,
                        lateAcceptanceReconfigurationSizeFactor));
            }
            // TODO - Remove it - static seed to make experiment predictable and run for more than one day
            experiment.setExperimentSeed(1727818432461L);
            experiment.setConfiguration(new DummyConfiguration("type", "vehiclerouting"));
            experiment.setConfiguration(new DummyConfiguration("datasetName", "cvrptw-25customers.json"));
            experiment.warmup(30L, 0.1);
            experiment.run(30L * 60L);
        }
    }

    public static void main(String[] args) {
        foragerParamCalibration();
        lateAcceptanceParamCalibration();
    }
}
