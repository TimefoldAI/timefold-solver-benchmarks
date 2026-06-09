package ai.timefold.solver.benchmarks.micro.common;

public record BenchmarkProperties(int forkCount, int batchSize, int warmupIterations, int measurementIterations,
        double relativeScoreErrorThreshold) {

}
