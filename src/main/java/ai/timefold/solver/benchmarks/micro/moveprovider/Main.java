package ai.timefold.solver.benchmarks.micro.moveprovider;

import java.io.IOException;
import java.io.InputStream;

import ai.timefold.solver.benchmarks.micro.common.AbstractMain;
import ai.timefold.solver.benchmarks.micro.moveprovider.jmh.BasicVariableMoveProviderBenchmark;
import ai.timefold.solver.benchmarks.micro.moveprovider.jmh.ListVariableMoveProviderBenchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;

public final class Main extends AbstractMain<Configuration> {

    public Main() {
        super("moveprovider");
    }

    @Override
    protected Configuration readConfiguration(InputStream inputStream) throws IOException {
        return Configuration.read(inputStream);
    }

    @Override
    protected Configuration getDefaultConfiguration() {
        return Configuration.getDefault();
    }

    public static void main(String[] args) throws RunnerException, IOException {
        new Main().run(args);
    }

    public void run(String[] args) throws RunnerException, IOException {
        var configuration = readConfiguration();
        var options = getBaseJmhConfig(configuration);
        options = processBasicMoveProviderBenchmark(options, configuration);
        options = processListMoveProviderBenchmark(options, configuration);
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
            var moveProviderCaseName = benchParams.getParam("basicMoveProvider");
            if (moveProviderCaseName == null) {
                moveProviderCaseName = benchParams.getParam("listMoveProvider");
            }
            var benchmarkName = benchParams.getBenchmark() + " " + moveProviderCaseName;
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

    private ChainedOptionsBuilder processBasicMoveProviderBenchmark(ChainedOptionsBuilder options,
            Configuration configuration) {
        var enabledCaseNames = configuration.getEnabledBasicMoveProviderCases().stream()
                .map(Enum::name)
                .toArray(String[]::new);
        LOGGER.info("Basic-variable move providers enabled: {}", (Object) enabledCaseNames);
        if (enabledCaseNames.length > 0) {
            options = options.include(BasicVariableMoveProviderBenchmark.class.getSimpleName())
                    .param("basicMoveProvider", enabledCaseNames);
        }
        return options;
    }

    private ChainedOptionsBuilder processListMoveProviderBenchmark(ChainedOptionsBuilder options,
            Configuration configuration) {
        var enabledCaseNames = configuration.getEnabledListMoveProviderCases().stream()
                .map(Enum::name)
                .toArray(String[]::new);
        LOGGER.info("List-variable move providers enabled: {}", (Object) enabledCaseNames);
        if (enabledCaseNames.length > 0) {
            options = options.include(ListVariableMoveProviderBenchmark.class.getSimpleName())
                    .param("listMoveProvider", enabledCaseNames);
        }
        return options;
    }

}
