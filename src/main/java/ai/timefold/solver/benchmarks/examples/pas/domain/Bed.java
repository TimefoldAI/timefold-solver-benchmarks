package ai.timefold.solver.benchmarks.examples.pas.domain;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Bed.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Bed extends AbstractPersistable {

    private Room room;
    private int indexInRoom;

    public Bed() {
    }

    public Bed(long id, Room room, int indexInRoom) {
        super(id);
        this.room = room;
        this.indexInRoom = indexInRoom;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public int getIndexInRoom() {
        return indexInRoom;
    }

    public void setIndexInRoom(int indexInRoom) {
        this.indexInRoom = indexInRoom;
    }

    @Override
    public String toString() {
        return room + "(" + indexInRoom + ")";
    }

}
