package ai.timefold.solver.benchmarks.competitive;

import java.time.Duration;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.impl.score.director.InnerScore;

public record Result<Dataset_ extends Dataset<Dataset_>, Score_ extends Score<Score_>>(Dataset_ dataset,
        InnerScore<Score_> score, int locationCount, int vehicleCount, Duration runtime) {
}
