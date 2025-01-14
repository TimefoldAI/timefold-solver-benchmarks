package ai.timefold.solver.benchmarks.micro.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Defaults;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.format.OutputFormat;
import org.openjdk.jmh.runner.format.OutputFormatFactory;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.util.UnCloseablePrintStream;
import org.openjdk.jmh.util.Utils;

public final class ResultCapturingJMHRunner extends Runner {

    public ResultCapturingJMHRunner(Path resultsDirectory, Options options) {
        super(options, new ResultCapturingOutputFormat(resultsDirectory, createOutputFormat(options)));
    }

    public List<File> getResults() {
        return ((ResultCapturingOutputFormat) out).results;
    }

    private static OutputFormat createOutputFormat(Options options) { // Copied from parent, as access is private.
        // sadly required here as the check cannot be made before calling this method in constructor
        if (options == null) {
            throw new IllegalArgumentException("Options not allowed to be null.");
        }

        PrintStream out;
        if (options.getOutput().hasValue()) {
            try {
                out = new PrintStream(options.getOutput().get());
            } catch (FileNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        } else {
            // Protect the System.out from accidental closing
            try {
                out = new UnCloseablePrintStream(System.out, Utils.guessConsoleEncoding());
            } catch (UnsupportedEncodingException ex) {
                throw new IllegalStateException(ex);
            }
        }

        return OutputFormatFactory.createFormatInstance(out, options.verbosity().orElse(Defaults.VERBOSITY));
    }

    private static final class ResultCapturingOutputFormat implements OutputFormat {

        private final Path resultsDirectory;
        private final OutputFormat delegate;
        private final List<File> results = new ArrayList<>();

        public ResultCapturingOutputFormat(Path resultsDirectory, OutputFormat format) {
            this.resultsDirectory = resultsDirectory;
            this.delegate = format;
        }

        @Override
        public void iteration(BenchmarkParams benchParams, IterationParams params, int iteration) {
            delegate.iteration(benchParams, params, iteration);
        }

        @Override
        public void iterationResult(BenchmarkParams benchParams, IterationParams params, int iteration, IterationResult data) {
            delegate.iterationResult(benchParams, params, iteration, data);
        }

        @Override
        public void startBenchmark(BenchmarkParams benchParams) {
            delegate.startBenchmark(benchParams);
        }

        @Override
        public void endBenchmark(BenchmarkResult result) {
            delegate.endBenchmark(result);
            var jfrFile = findJfrFile(resultsDirectory.toFile());
            if (jfrFile == null) {
                return;
            }
            var target = resultsDirectory.resolve(System.nanoTime() + "-" + jfrFile.getName());
            try {
                Files.copy(jfrFile.toPath(), target);
                results.add(jfrFile);
            } catch (IOException e) {
                verbosePrintln("Failed to copy JFR file: " + e.getMessage());
            }
        }

        public static File findJfrFile(File file) {
            if (file.isDirectory()) {
                for (var f : file.listFiles()) {
                    if (findJfrFile(f) != null) {
                        return f;
                    }
                }
                return null;
            } else {
                if (file.getName().endsWith(".jfr")) {
                    return file;
                } else {
                    return null;
                }
            }
        }

        @Override
        public void startRun() {
            delegate.startRun();
        }

        @Override
        public void endRun(Collection<RunResult> result) {
            delegate.endRun(result);
        }

        @Override
        public void print(String s) {
            delegate.print(s);
        }

        @Override
        public void println(String s) {
            delegate.println(s);
        }

        @Override
        public void flush() {
            delegate.flush();
        }

        @Override
        public void close() {
            delegate.close();
        }

        @Override
        public void verbosePrintln(String s) {
            delegate.verbosePrintln(s);
        }

        @Override
        public void write(int b) {
            delegate.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            delegate.write(b);
        }
    }

}
