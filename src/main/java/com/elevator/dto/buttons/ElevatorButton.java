package com.elevator.dto.buttons;

import com.elevator.exception.ElevatorSystemException;
import lombok.Getter;
import lombok.Setter;

public enum ElevatorButton {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    STOP(0);

    @Getter
    private int value;

    @Getter
    @Setter
    private boolean active;

    ElevatorButton(int value) {
        this.value = value;
    }

    public static ElevatorButton getButtonByValue(int value) throws ElevatorSystemException {
        for (ElevatorButton elevatorButton : ElevatorButton.values()) {
            if (elevatorButton.getValue() == value) {
                return elevatorButton;
            }
        }

        throw new ElevatorSystemException("Сбой в системе движения лифта. Невозможно определить кнопку лифта по значению " + value);
    }
}
