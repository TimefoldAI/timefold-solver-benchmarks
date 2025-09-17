package ai.timefold.solver.benchmarks.examples.tsp.domain;

import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;
import ai.timefold.solver.benchmarks.examples.tsp.domain.location.LocationAware;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Domicile.class, name = "domicile"),
        @JsonSubTypes.Type(value = Visit.class, name = "visit"),
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
@PlanningEntity
public abstract class Standstill implements LocationAware {

    private long id;
    private Visit nextStandstill;

    Standstill() {
    }

    protected Standstill(long id) {
        this.id = id;
    }

    @PlanningId
    public long getId() {
        return id;
    }

    public abstract Standstill getPreviousStandstill();

    public abstract long getDistanceFromPreviousStandstill();

    @InverseRelationShadowVariable(sourceVariableName = "previousStandstill")
    public Visit getNextStandstill() {
        return nextStandstill;
    }

    public void setNextStandstill(Visit nextStandstill) {
        this.nextStandstill = nextStandstill;
    }

    /**
     * @param standstill never null
     * @return a positive number, the distance multiplied by 1000 to avoid floating point arithmetic rounding errors
     */
    @JsonIgnore
    public abstract long getDistanceTo(Standstill standstill);

}
