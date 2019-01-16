package com.elevator.dto.buttons;

import lombok.Getter;
import lombok.Setter;

public enum ElevatorButton {
    ONE(1, "первый этаж"),
    TEN(10, "последний этаж"),
    STOP(0, "STOP");

    @Getter
    private int floorValue;

    @Getter
    private String name;

    @Getter
    @Setter
    private boolean active;

    ElevatorButton(int floorValue, String name) {
        this.floorValue = floorValue;
        this.name = name;
    }

    public static ElevatorButton getButtonByValue(int value) {
        for (ElevatorButton elevatorButton : ElevatorButton.values()) {
            if (elevatorButton.getFloorValue() == value) {
                return elevatorButton;
            }
        }

        return null;
    }
}
