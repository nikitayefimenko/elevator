package com.elevator.dto.buttons;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FloorButton {
    private Direction direction;
    private boolean active;
}
