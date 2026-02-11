package ai.timefold.solver.benchmarks.examples.tsp.domain;

import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.benchmarks.examples.tsp.domain.location.Location;
import ai.timefold.solver.benchmarks.examples.tsp.domain.location.LocationAware;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;

@PlanningEntity
public class Tour implements LocationAware {

    private Domicile domicile;
    @PlanningListVariable
    private List<Visit> visits;

    public Tour() {
        this.visits = new ArrayList<>();
    }

    public Tour(int size) {
        this.visits = new ArrayList<>(size);
    }

    public Tour(int size, Domicile domicile) {
        this(size);
        this.domicile = domicile;
    }

    public Domicile getDomicile() {
        return domicile;
    }

    public void setDomicile(Domicile domicile) {
        this.domicile = domicile;
    }

    public List<Visit> getVisits() {
        return visits;
    }

    public void setVisits(List<Visit> visits) {
        this.visits = visits;
    }

    @Override
    public Location getLocation() {
        return domicile.getLocation();
    }
}
