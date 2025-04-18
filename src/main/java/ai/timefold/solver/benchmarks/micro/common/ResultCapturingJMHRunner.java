package ai.timefold.solver.benchmarks.micro.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

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

/**
 * The JFR file from one fork will overwrite JFR files from previous forks.
 * But we want all of them to be saved.
 * Therefore we need to break into JMH, and save the JFR file after each fork.
 * Because JMH doesn't expose when a fork is finished, we need to count iterations.
 */
public final class ResultCapturingJMHRunner extends Runner {

    public ResultCapturingJMHRunner(Path resultsDirectory, Options options) {
        super(options, new ResultCapturingOutputFormat(resultsDirectory, createOutputFormat(options)));
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
        private int expectedIterationCount = -1;
        private int iterationsRemaining = -1;

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
            iterationsRemaining--;
            if (iterationsRemaining == 0) {
                iterationsRemaining = expectedIterationCount;
                var jfrFile = findJfrFile(resultsDirectory.toFile());
                if (jfrFile == null) {
                    return;
                }
                var unixTime = System.currentTimeMillis() / 1000;
                var target = resultsDirectory.resolve(unixTime + "-" + jfrFile.getName());
                try {
                    Files.copy(jfrFile.toPath(), target);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        private static File findJfrFile(File file) {
            if (file.isDirectory()) {
                for (var f : file.listFiles()) {
                    var result = findJfrFile(f);
                    if (result != null) {
                        return result;
                    }
                }
            } else if (file.getName().startsWith("jfr-") && file.getName().endsWith(".jfr")) {
                // Only match the jfr-cpu.jfr file produced by JMH.
                // Otherwise the previously copied files could also be matched.
                return file;
            }
            return null;
        }

        @Override
        public void startBenchmark(BenchmarkParams benchParams) {
            expectedIterationCount = benchParams.getWarmup().getCount() + benchParams.getMeasurement().getCount();
            iterationsRemaining = expectedIterationCount;
            delegate.startBenchmark(benchParams);
        }

        @Override
        public void endBenchmark(BenchmarkResult result) {
            delegate.endBenchmark(result);
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
