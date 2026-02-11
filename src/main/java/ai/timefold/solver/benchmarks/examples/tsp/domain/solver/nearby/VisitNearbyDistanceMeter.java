package ai.timefold.solver.benchmarks.examples.tsp.domain.solver.nearby;

import ai.timefold.solver.benchmarks.examples.tsp.domain.location.LocationAware;
import ai.timefold.solver.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;

public class VisitNearbyDistanceMeter implements NearbyDistanceMeter<LocationAware, LocationAware> {

    @Override
    public double getNearbyDistance(LocationAware origin, LocationAware destination) {
        return origin.getLocation().getDistanceTo(destination.getLocation());
    }

}
