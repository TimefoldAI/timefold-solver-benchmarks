package ai.timefold.solver.benchmarks.micro.factorial.configuration;

public interface ExperimentWriter {

    void log(String message);

    void saveResult(String result);
}
