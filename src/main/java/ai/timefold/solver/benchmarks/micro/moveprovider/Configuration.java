package ai.timefold.solver.benchmarks.micro.moveprovider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import ai.timefold.solver.benchmarks.micro.common.AbstractConfiguration;

final class Configuration extends AbstractConfiguration {

    public static Configuration read(InputStream inputStream) throws IOException {
        var properties = new Properties();
        properties.load(inputStream);

        var enabledBasicMoveProviderCases =
                parseExamples(properties.getProperty("basic_move_provider"), BasicMoveProviderCase.values());
        var enabledListMoveProviderCases =
                parseExamples(properties.getProperty("list_move_provider"), ListMoveProviderCase.values());
        var benchmarkProperties = readBenchmarkProperties(properties, getDefault());
        return new Configuration(enabledBasicMoveProviderCases, enabledListMoveProviderCases,
                benchmarkProperties.forkCount(), benchmarkProperties.warmupIterations(),
                benchmarkProperties.measurementIterations(), benchmarkProperties.relativeScoreErrorThreshold());
    }

    public static Configuration getDefault() {
        return new Configuration(Arrays.asList(BasicMoveProviderCase.values()), Arrays.asList(ListMoveProviderCase.values()),
                DEFAULT_FORK_COUNT, DEFAULT_WARMUP_ITERATIONS, DEFAULT_MEASUREMENT_ITERATIONS,
                DEFAULT_RELATIVE_SCORE_ERROR_THRESHOLD);
    }

    private final List<BasicMoveProviderCase> enabledBasicMoveProviderCases;
    private final List<ListMoveProviderCase> enabledListMoveProviderCases;

    private Configuration(List<BasicMoveProviderCase> enabledBasicMoveProviderCases,
            List<ListMoveProviderCase> enabledListMoveProviderCases, int forkCount, int warmupIterations,
            int measurementIterations, double relativeScoreErrorThreshold) {
        super(forkCount, warmupIterations, measurementIterations, relativeScoreErrorThreshold);
        this.enabledBasicMoveProviderCases = enabledBasicMoveProviderCases;
        this.enabledListMoveProviderCases = enabledListMoveProviderCases;
    }

    public List<BasicMoveProviderCase> getEnabledBasicMoveProviderCases() {
        return enabledBasicMoveProviderCases;
    }

    public List<ListMoveProviderCase> getEnabledListMoveProviderCases() {
        return enabledListMoveProviderCases;
    }

}
