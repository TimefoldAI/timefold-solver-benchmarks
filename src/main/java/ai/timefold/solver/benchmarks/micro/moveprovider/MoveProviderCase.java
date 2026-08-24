package ai.timefold.solver.benchmarks.micro.moveprovider;

import ai.timefold.solver.core.preview.api.domain.metamodel.PlanningSolutionMetaModel;
import ai.timefold.solver.core.preview.api.neighborhood.MoveProvider;

/**
 * A single case in {@code BasicMoveProviderCase} or {@code ListMoveProviderCase}: one built-in
 * {@link MoveProvider} constructed against one {@link Example}. Implemented by both enums so
 * {@code MoveProviderProblem} can run either without knowing which.
 */
public interface MoveProviderCase {

    Example getExample();

    MoveProvider<?> createMoveProvider(PlanningSolutionMetaModel<?> solutionMetaModel);

}
