package ai.timefold.solver.benchmarks.micro.moveprovider;

import ai.timefold.solver.core.preview.api.domain.metamodel.PlanningSolutionMetaModel;
import ai.timefold.solver.core.preview.api.move.Move;
import ai.timefold.solver.core.preview.api.neighborhood.MoveProvider;

/**
 * A single case in {@code BasicMoveProviderCase} or {@code ListMoveProviderCase}: one built-in
 * {@link MoveProvider} constructed against one {@link Example}. Implemented by both enums so
 * {@code MoveProviderProblem} can run either without knowing which.
 */
public interface MoveProviderCase {

    Example getExample();

    MoveProvider<?> createMoveProvider(PlanningSolutionMetaModel<?> solutionMetaModel);

    /**
     * The quantity of work in one move, in variable writes, used to normalize throughput in the CI
     * report; see {@code MovedValueCounter}.
     * <p>
     * Runs on the measured path, so it must not allocate: read a size the move already holds, such
     * as {@code Sample.size()} or {@code Range.length()}. Never call
     * {@link Move#getPlanningEntities()} or {@link Move#getPlanningValues()} here; every built-in
     * implementation of both allocates a collection.
     *
     * @param move a move this case's provider produced
     */
    int countMovedValues(Move<?> move);

}
