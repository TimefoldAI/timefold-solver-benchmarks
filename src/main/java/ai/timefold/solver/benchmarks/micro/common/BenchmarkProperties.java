package ai.timefold.solver.benchmarks.micro.common;

public record BenchmarkProperties(int forkCount, int warmupIterations, int measurementIterations,
        double relativeScoreErrorThreshold) {

}
