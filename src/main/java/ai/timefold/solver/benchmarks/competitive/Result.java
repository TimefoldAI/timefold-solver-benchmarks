package ai.timefold.solver.benchmarks.competitive;

import java.time.Duration;

import ai.timefold.solver.core.api.score.Score;

public record Result<Dataset_ extends Dataset, Score_ extends Score<Score_>>(Dataset_ dataset, Score_ score, int locationCount,
        int vehicleCount, Duration runtime) {
}
