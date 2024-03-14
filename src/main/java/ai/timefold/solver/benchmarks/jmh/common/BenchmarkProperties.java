package ai.timefold.solver.benchmarks.jmh.common;

public record BenchmarkProperties(int forkCount, int warmupIterations, int measurementIterations,
        double relativeScoreErrorThreshold) {

}
