package ai.timefold.solver.benchmarks.micro.moveprovider.jmh;

import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Counts the values that the drawn moves change, so the CI report can normalize throughput into a
 * cost for each value; see {@code summarize-moveprovider.py}. Without it, a reader ranks the move
 * providers by raw throughput and investigates the slowest one, even though that provider is only
 * slow because one of its moves does the work of ten.
 *
 * <p>
 * A separate state class because {@link AuxCounters} requires {@link Scope#Thread}, while
 * {@link AbstractMoveProviderBenchmark} is {@link Scope#Benchmark}. The benchmark runs one thread.
 *
 * <p>
 * {@link AuxCounters.Type#OPERATIONS} reports the counter as a rate, in the same unit as the
 * primary metric, so the report divides the two to get the values for each operation:
 * {@code secondaryMetrics.movedValues.score / primaryMetric.score}.
 * {@link AuxCounters.Type#EVENTS} would report a raw total instead, which needs the iteration count
 * and duration to normalize. JMH resets the field for each iteration, so no {@code @Setup} is
 * needed.
 */
@State(Scope.Thread)
@AuxCounters(AuxCounters.Type.OPERATIONS)
public class MovedValueCounter {

    public long movedValues;

}
