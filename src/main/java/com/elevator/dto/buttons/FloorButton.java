package com.elevator.dto.buttons;


import lombok.Data;

@Data
public class FloorButton {

    public FloorButton(Direction direction, String name) {
        this.direction = direction;
        this.name = name;
    }

    private Direction direction;
    private boolean active;
    private String name;
}
