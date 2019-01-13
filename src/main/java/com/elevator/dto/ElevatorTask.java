package com.elevator.dto;

import lombok.Data;

@Data
public class ElevatorTask implements Comparable<ElevatorTask> {

    public ElevatorTask(Floor destinationFloor, int priorityNum) {
        this.destinationFloor = destinationFloor;
        this.priorityNum = priorityNum;
    }

    private Floor destinationFloor;
    private int priorityNum;

    @Override
    public int compareTo(ElevatorTask o) {
        return this.priorityNum - o.getPriorityNum();
    }

}
