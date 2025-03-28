package ai.timefold.solver.benchmarks.examples.machinereassignment.domain;

import java.util.List;
import java.util.Map;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.KeySerializer;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class MrMachine extends AbstractPersistable {

    private MrNeighborhood neighborhood;
    private MrLocation location;

    // Order is equal to resourceList so resource.getIndex() can be used
    private List<MrMachineCapacity> machineCapacityList;

    @JsonIdentityReference(alwaysAsId = true)
    @JsonSerialize(keyUsing = KeySerializer.class)
    @JsonDeserialize(keyUsing = MrMachineKeyDeserializer.class)
    private Map<MrMachine, Integer> machineMoveCostMap; // key is toMachine

    @SuppressWarnings("unused")
    MrMachine() {
    }

    public MrMachine(long id) {
        super(id);
    }

    public MrMachine(long id, MrLocation location) {
        super(id);
        this.location = location;
    }

    public MrNeighborhood getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(MrNeighborhood neighborhood) {
        this.neighborhood = neighborhood;
    }

    public MrLocation getLocation() {
        return location;
    }

    public void setLocation(MrLocation location) {
        this.location = location;
    }

    public List<MrMachineCapacity> getMachineCapacityList() {
        return machineCapacityList;
    }

    public void setMachineCapacityList(List<MrMachineCapacity> machineCapacityList) {
        this.machineCapacityList = machineCapacityList;
    }

    public MrMachineCapacity getMachineCapacity(MrResource resource) {
        return machineCapacityList.get(resource.getIndex());
    }

    public Map<MrMachine, Integer> getMachineMoveCostMap() {
        return machineMoveCostMap;
    }

    public void setMachineMoveCostMap(Map<MrMachine, Integer> machineMoveCostMap) {
        this.machineMoveCostMap = machineMoveCostMap;
    }

    public int getMoveCostTo(MrMachine toMachine) {
        return machineMoveCostMap.get(toMachine);
    }

}
