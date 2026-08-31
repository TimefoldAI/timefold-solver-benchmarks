package ai.timefold.solver.benchmarks.micro.moveprovider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.micro.moveprovider.jmh.AbstractMoveProviderBenchmark;
import ai.timefold.solver.benchmarks.micro.moveprovider.jmh.MovedValueCounter;
import ai.timefold.solver.benchmarks.micro.moveprovider.problems.MoveProviderProblem;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openjdk.jmh.infra.Blackhole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The runnable check for every one of the 25 built-in move providers: run the whole lifecycle once
 * for both scenarios and assert that each produced a move. Catches a provider that rejects its
 * metamodel, a provider that only produces null moves, a case whose {@code countMovedValues} cast no
 * longer matches its provider's move, and any lifecycle mistake, without JMH.
 *
 * <p>
 * Reads exactly the datasets the benchmarks read, through the same {@link MoveProviderCase}
 * enums, and uses the shared scenario constants from {@link AbstractMoveProviderBenchmark} rather
 * than its own numbers, so the test and the benchmarks cannot drift apart.
 */
final class MoveProviderBenchmarkTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoveProviderBenchmarkTest.class);

    @ParameterizedTest
    @MethodSource("moveProviderCaseProvider")
    void runTest(MoveProviderCase moveProviderCase) {
        LOGGER.info("Testing {}.", moveProviderCase);
        var blackhole =
                new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
        var counter = new MovedValueCounter();

        var problem = new MoveProviderProblem<>(moveProviderCase);
        problem.setupTrial();
        problem.setupIteration();
        var commitMoveResult = problem.runCommitMove(AbstractMoveProviderBenchmark.MAX_DRAW_ATTEMPTS_PER_MOVE,
                blackhole, counter);
        assertThat(commitMoveResult).isNotNull();
        problem.tearDownIteration();

        problem.setupIteration();
        var drawOnlyResult = problem.runDrawOnly(AbstractMoveProviderBenchmark.DRAW_ONLY_DRAWS,
                AbstractMoveProviderBenchmark.MAX_DRAW_ATTEMPTS_PER_MOVE, blackhole, counter);
        assertThat(drawOnlyResult).isNotNull();
        problem.tearDownIteration();

        // One value per draw at the very least; a wrong cast in the enum throws before we get here.
        // The drawOnly leg is also the only check that a provider can produce DRAW_ONLY_DRAWS moves
        // from one static solution: a bi move iterator retires dead lefts as the invocation runs, so
        // a provider that runs dry throws out of drawMove() rather than failing this assert.
        assertThat(counter.movedValues)
                .isGreaterThanOrEqualTo(1 + AbstractMoveProviderBenchmark.DRAW_ONLY_DRAWS);

        problem.teardownTrial();
    }

    public static Stream<MoveProviderCase> moveProviderCaseProvider() {
        return Stream.concat(Stream.of(BasicMoveProviderCase.values()), Stream.of(ListMoveProviderCase.values()));
    }

}
