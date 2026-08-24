package ai.timefold.solver.benchmarks.micro.moveprovider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import ai.timefold.solver.benchmarks.micro.moveprovider.jmh.AbstractMoveProviderBenchmark;
import ai.timefold.solver.benchmarks.micro.moveprovider.problems.MoveProviderProblem;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openjdk.jmh.infra.Blackhole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The runnable check for every one of the 25 built-in move providers, per
 * {@code /home/agent/.claude/plans/zany-drifting-ocean.md}: run the whole lifecycle once for both
 * scenarios and assert that a move was actually committed. Catches a provider that rejects its
 * metamodel, a provider that only produces null moves, and any lifecycle mistake, without JMH.
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

        var problem = new MoveProviderProblem<>(moveProviderCase);
        problem.setupTrial();
        problem.setupIteration();
        var fewStepsManyMovesResult = problem.runInvocation(AbstractMoveProviderBenchmark.FEW_STEP_COUNT,
                AbstractMoveProviderBenchmark.MANY_MOVES_PER_STEP, AbstractMoveProviderBenchmark.MAX_DRAW_ATTEMPTS_PER_MOVE,
                blackhole);
        assertThat(fewStepsManyMovesResult).isNotNull();
        problem.tearDownIteration();

        problem.setupIteration();
        var manyStepsFewMovesResult = problem.runInvocation(AbstractMoveProviderBenchmark.MANY_STEP_COUNT,
                AbstractMoveProviderBenchmark.FEW_MOVES_PER_STEP, AbstractMoveProviderBenchmark.MAX_DRAW_ATTEMPTS_PER_MOVE,
                blackhole);
        assertThat(manyStepsFewMovesResult).isNotNull();
        problem.tearDownIteration();

        problem.teardownTrial();
    }

    public static Stream<MoveProviderCase> moveProviderCaseProvider() {
        return Stream.concat(Stream.of(BasicMoveProviderCase.values()), Stream.of(ListMoveProviderCase.values()));
    }

}
