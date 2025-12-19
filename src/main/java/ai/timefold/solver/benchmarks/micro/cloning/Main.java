package ai.timefold.solver.benchmarks.micro.cloning;

import java.io.IOException;
import java.io.InputStream;

import ai.timefold.solver.benchmarks.micro.cloning.jmh.CloningBenchmark;
import ai.timefold.solver.benchmarks.micro.common.AbstractMain;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;

public final class Main extends AbstractMain<Configuration> {

    public Main() {
        super("cloning");
    }

    @Override
    protected Configuration readConfiguration(InputStream inputStream) throws IOException {
        return Configuration.read(inputStream);
    }

    @Override
    protected Configuration getDefaultConfiguration() {
        return Configuration.getDefault();
    }

    public static void main(String[] args) throws RunnerException {
        new Main().run(args);
    }

    public void run(String[] args) throws RunnerException {
        var configuration = readConfiguration();
        var options = getBaseJmhConfig(configuration);
        options = processBenchmark(options, configuration);
        options = initAsyncProfiler(options);

        var runner = new Runner(options.build());
        var runResults = runner.run();
        convertJfrToFlameGraphs();

        var relativeScoreErrorThreshold = configuration.getRelativeScoreErrorThreshold();
        var thresholdForPrint = ((int) Math.round(relativeScoreErrorThreshold * 10_000)) / 100.0D;
        runResults.forEach(result -> {
            var primaryResult = result.getPrimaryResult();
            var score = primaryResult.getScore();
            var scoreError = primaryResult.getScoreError();
            var relativeScoreError = scoreError / score;

            var benchParams = result.getParams();
            var benchmarkName = benchParams.getBenchmark() + " " + benchParams.getParam("example");
            var relativeScoreErrorForPrint = ((int) Math.round(relativeScoreError * 10_000)) / 100.0D;
            if (relativeScoreError > relativeScoreErrorThreshold) {
                LOGGER.warn("Score error for '{}' is too high: ± {} % (threshold: ± {} %).", benchmarkName,
                        relativeScoreErrorForPrint, thresholdForPrint);
            } else if (relativeScoreError > (relativeScoreErrorThreshold * 0.9)) {
                LOGGER.info("Score error for '{}' approaching threshold: ± {} % (threshold: ± {} %).", benchmarkName,
                        relativeScoreErrorForPrint, thresholdForPrint);
            }
        });
    }

    private ChainedOptionsBuilder processBenchmark(ChainedOptionsBuilder options, Configuration configuration) {
        var supportedExampleNames = getSupportedExampleNames(configuration);
        if (supportedExampleNames.length > 0) {
            options = options.include(CloningBenchmark.class.getSimpleName())
                    .param("example", supportedExampleNames);
        }
        return options;
    }

    private String[] getSupportedExampleNames(Configuration configuration) {
        var examples = configuration.getEnabledExamples()
                .stream()
                .map(Enum::name)
                .toArray(String[]::new);
        LOGGER.info("Examples enabled: {}", (Object) examples);
        return examples;
    }

}
