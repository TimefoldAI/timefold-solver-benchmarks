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
import java.util.Properties;

import org.openjdk.jmh.profile.AsyncProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMain<C extends AbstractConfiguration> {

    private static final Logger STATIC_LOGGER = LoggerFactory.getLogger(AbstractMain.class);

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

    protected static Optional<Path> getAsyncProfilerPath() {
        try {
            var properties = new Properties();
            properties.load(AbstractMain.class.getResourceAsStream("/buildtime.properties"));
            var asyncProfilerPath = Path.of(
                    properties.getProperty("async.profiler.path").trim(),
                    "lib",
                    "libasyncProfiler.so")
                    .toAbsolutePath();
            if (!asyncProfilerPath.toFile().exists()) {
                return Optional.empty();
            }
            return Optional.of(asyncProfilerPath);
        } catch (IOException e) {
            STATIC_LOGGER.error("Failed reading buildtime.properties from the benchmarks JAR.", e);
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
                                    "libPath=" + asyncProfilerPath);
                }).orElseGet(() -> {
                    LOGGER.warn("Async profiler not found.");
                    return options;
                });
    }

    protected void convertJfrToFlameGraphs() {
        if (getAsyncProfilerPath().isPresent()) {
            var combinedJfr = resultsDirectory.resolve("combined.jfr");
            var jfrAssembleLog = resultsDirectory.resolve("jfr-assemble.log");
            try {
                // Merge all the JFR files that were generated by JMH and copied over by our custom JMH runner.
                var process = new ProcessBuilder()
                        .command("jfr", "assemble", resultsDirectory.toAbsolutePath().toString(),
                                combinedJfr.toAbsolutePath().toString())
                        .redirectOutput(jfrAssembleLog.toFile())
                        .start();
                if (process.waitFor() != 0) {
                    LOGGER.error("Failed combining JFR files. See '{}' for details.", jfrAssembleLog);
                    return;
                }
                // From the combined JMH file, create flame graphs.
                generateFlameGraphsFromJfr(combinedJfr, null);
                generateFlameGraphsFromJfr(combinedJfr, "alloc");
            } catch (Exception e) {
                LOGGER.error("Failed converting JFR to flame graphs.", e);
            }
        } else {
            LOGGER.info("Skipping JFR conversion in '{}'.", resultsDirectory);
        }
    }

    private static void generateFlameGraphsFromJfr(Path jfrFilePath, String type) {
        var args = type == null ? new String[] {
                "--simple",
                jfrFilePath.toString(),
                Path.of(jfrFilePath.toAbsolutePath().getParent().toString(), "cpu.html").toString()
        }
                : new String[] {
                        "--simple",
                        "--" + type,
                        jfrFilePath.toString(),
                        Path.of(jfrFilePath.toAbsolutePath().getParent().toString(), type + ".html").toString()
                };
        try { // Converter is stupidly in the default package.
            var fooClass = Class.forName("jfr2flame");
            var fooMethod = fooClass.getMethod("main", String[].class);
            fooMethod.invoke(null, (Object) args);
            STATIC_LOGGER.info("Generating flame graph succeeded: {}.", Arrays.toString(args));
        } catch (Exception ex) {
            STATIC_LOGGER.error("Generating flame graph failed: {}.", Arrays.toString(args), ex);
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
                .jvmArgs("-XX:+UseParallelGC", "-Xmx2g") // Throughput-focused GC.
                .result(resultsDirectory.resolve("results.json").toAbsolutePath().toString())
                .resultFormat(ResultFormatType.JSON)
                .shouldDoGC(true);
    }

}
