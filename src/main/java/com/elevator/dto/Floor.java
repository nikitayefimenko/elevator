package com.elevator.dto;

import com.elevator.dto.buttons.Direction;
import com.elevator.dto.buttons.FloorButton;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Floor {
    FIRST(1, Collections.singletonList(new FloorButton(Direction.UP, false))),
    SECOND(2, Arrays.asList(new FloorButton(Direction.UP, false), new FloorButton(Direction.DOWN, false))),
    THIRD(3, Arrays.asList(new FloorButton(Direction.UP, false), new FloorButton(Direction.DOWN, false))),
    FOURTH(4, Collections.singletonList(new FloorButton(Direction.DOWN, false)));

    @Getter
    private int number;

    @Getter
    private List<FloorButton> floorButtons;

    @Getter
    private List<Person> waitingPersons = new ArrayList<>();

    Floor(int number, List<FloorButton> floorButtons) {
        this.number = number;
        this.floorButtons = floorButtons;
    }

    public boolean isButtonActive(Direction direction) {
        for (FloorButton floorButton : this.floorButtons) {
            if (floorButton.getDirection() == direction) {
                return floorButton.isActive();
            }
        }

        return false;
    }

    public void activateButton(Direction direction) {
        for (FloorButton floorButton : this.floorButtons) {
            if (floorButton.getDirection() == direction) {
                floorButton.setActive(true);
            }
        }
    }

    public void deactivateAllButtons() {
        for (FloorButton floorButton : this.floorButtons) {
            floorButton.setActive(false);
        }
    }

    public static Floor getFloorByNumber(int number) {
        for (Floor floor : Floor.values()) {
            if (floor.getNumber() == number) {
                return floor;
            }
        }

        return null;
    }

    public void addPersonToFloor(Person person) {
        this.waitingPersons.add(person);
    }

}
