package ai.timefold.solver.benchmarks.examples.tsp.domain;

import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;
import ai.timefold.solver.benchmarks.examples.tsp.domain.location.Location;
import ai.timefold.solver.benchmarks.examples.tsp.domain.location.LocationAware;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;

@PlanningEntity
@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class Tour extends AbstractPersistable implements LocationAware {

    private Domicile domicile;
    @PlanningListVariable
    private List<Visit> visitList;

    public Tour() {
        super(1L);
        this.visitList = new ArrayList<>();
    }

    public Tour(int size) {
        this(size, null);
    }

    public Tour(int size, Domicile domicile) {
        super(1L);
        this.visitList = new ArrayList<>(size);
        this.domicile = domicile;
    }

    public Domicile getDomicile() {
        return domicile;
    }

    public void setDomicile(Domicile domicile) {
        this.domicile = domicile;
    }

    public List<Visit> getVisitList() {
        return visitList;
    }

    public void setVisitList(List<Visit> visitList) {
        this.visitList = visitList;
    }

    @Override
    public Location getLocation() {
        return domicile.getLocation();
    }
}
