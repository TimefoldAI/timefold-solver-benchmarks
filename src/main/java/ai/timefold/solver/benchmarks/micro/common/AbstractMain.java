/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package ai.timefold.solver.benchmarks.micro.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.openjdk.jmh.profile.AsyncProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.convert.Arguments;
import one.profiler.AsyncProfilerLoader;

public abstract class AbstractMain<C extends AbstractConfiguration> {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final String subpackage;
    protected final Path resultsDirectory;

    protected AbstractMain(String subpackage) {
        this.subpackage = subpackage;
        var runId = Objects.requireNonNullElse(System.getenv("RUN_ID"), getTimestamp())
                .strip();
        this.resultsDirectory = Path.of("results", subpackage, runId);
        resultsDirectory.toFile().mkdirs();
    }

    private static String leftPad(int input, int length) {
        return String.format("%1$" + length + "s", input)
                .replace(' ', '0');
    }

    protected static String getTimestamp() {
        var now = Instant.now().atZone(ZoneId.systemDefault());
        var year = leftPad(now.getYear(), 4);
        var month = leftPad(now.getMonthValue(), 2);
        var day = leftPad(now.getDayOfMonth(), 2);
        var hour = leftPad(now.getHour(), 2);
        var minute = leftPad(now.getMinute(), 2);
        var second = leftPad(now.getSecond(), 2);
        return year + month + day + "_" + hour + minute + second;
    }

    protected Optional<Path> getAsyncProfilerPath() {
        try {
            return Optional.of(AsyncProfilerLoader.getAsyncProfilerPath());
        } catch (Exception e) {
            LOGGER.error("Failed loading AsyncProfiler.", e);
            return Optional.empty();
        }
    }

    protected ChainedOptionsBuilder initAsyncProfiler(ChainedOptionsBuilder options) {
        return getAsyncProfilerPath()
                .map(asyncProfilerPath -> {
                    LOGGER.info("Using Async profiler from {}.", asyncProfilerPath);
                    return options.addProfiler(AsyncProfiler.class,
                            "event=cpu;alloc;" +
                                    "output=jfr;" +
                                    "dir=" + resultsDirectory.toAbsolutePath() + ";" +
                                    "libPath=" + asyncProfilerPath + ";" +
                                    "rawCommand=features=vtable");
                }).orElseGet(() -> {
                    LOGGER.warn("Async profiler not found.");
                    return options;
                });
    }

    protected void convertJfrToFlameGraphs() {
        if (getAsyncProfilerPath().isPresent()) {
            try {
                Files.walk(resultsDirectory)
                        .filter(Files::isRegularFile)
                        .filter(f -> f.toString().endsWith(".jfr"))
                        .forEach(path -> {
                            LOGGER.info("Found JFR file: {}.", path);
                            for (var visualizationType : VisualizationType.values()) {
                                for (var dataType : DataType.values()) {
                                    visualizeJfr(path, visualizationType, dataType);
                                }
                            }
                        });
            } catch (IOException e) {
                LOGGER.error("Failed converting JFR to flame graphs.", e);
            }
        } else {
            LOGGER.warn("Skipping JFR conversion in '{}'.", resultsDirectory);
        }
    }

    private void visualizeJfr(Path jfrFilePath, VisualizationType visualizationType, DataType dataType) {
        var inputPath = jfrFilePath.toAbsolutePath();
        var filename = String.format("%s-%s.html", dataType.name, visualizationType.name);
        var output = Path.of(inputPath.getParent().toString(), filename);
        var argStream = Stream.of(
                "--simple", // Shorter names.
                // "--norm" removes random strings from lambdas; 
                //          allows to merge different frames which are only different
                //          because they use a different instance of the same lambda.
                "--norm",
                // "--skip 15" removes bottom frames which come from JMH; they are unnecessary clutter.
                "--skip", "15",
                "--" + dataType.name);
        var args = argStream.toArray(String[]::new);
        try {
            if (visualizationType == VisualizationType.FLAME_GRAPH) {
                one.convert.JfrToFlame.convert(inputPath.toString(), output.toString(), new Arguments(args));
            } else if (visualizationType == VisualizationType.HEAT_MAP) {
                one.convert.JfrToHeatmap.convert(inputPath.toString(), output.toString(), new Arguments(args));
            } else {
                throw new IllegalArgumentException("Unsupported visualization: " + visualizationType);
            }
            LOGGER.info("{} Generation succeeded: {}.", visualizationType, Arrays.toString(args));
        } catch (Exception ex) {
            LOGGER.error("{} Generation failed: {}.", visualizationType, Arrays.toString(args), ex);
        }
    }

    protected C readConfiguration() {
        var configPath = Path.of(subpackage + "-benchmark.properties").toAbsolutePath();
        if (configPath.toFile().exists()) {
            LOGGER.info("Using benchmark configuration file: {}.", configPath);
            try (var inputStream = Files.newInputStream(configPath)) {
                return readConfiguration(inputStream);
            } catch (IOException e) {
                throw new IllegalStateException("Failed reading benchmark properties: " + configPath, e);
            }
        } else {
            LOGGER.info("Using default benchmark configuration.");
            return getDefaultConfiguration();
        }
    }

    abstract protected C readConfiguration(InputStream inputStream) throws IOException;

    abstract protected C getDefaultConfiguration();

    public ChainedOptionsBuilder getBaseJmhConfig(C configuration) {
        return new OptionsBuilder()
                .forks(configuration.getForkCount())
                .warmupIterations(configuration.getWarmupIterations())
                .measurementIterations(configuration.getMeasurementIterations())
                .jvmArgs("-XX:+UseParallelGC", "-Xms4g", "-Xmx4g") // Throughput-focused GC.
                .result(resultsDirectory.resolve("results.json").toAbsolutePath().toString())
                .resultFormat(ResultFormatType.JSON)
                .shouldDoGC(true);
    }

    private enum VisualizationType {

        FLAME_GRAPH("flamegraph"),
        HEAT_MAP("heatmap");

        private final String name;

        VisualizationType(String name) {
            this.name = Objects.requireNonNull(name);
        }

    }

    private enum DataType {

        CPU("cpu"),
        MEM("alloc");

        private final String name;

        DataType(String name) {
            this.name = Objects.requireNonNull(name);
        }

    }

}
