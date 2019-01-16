package com.elevator.dto;

import com.elevator.dto.buttons.Direction;
import com.elevator.dto.buttons.FloorButton;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.elevator.dto.PanelType.DOUBLE;
import static com.elevator.dto.PanelType.SINGLE;

public enum Floor {
    FIRST(1, Arrays.asList(
            new FloorPanel(Collections.singletonList(new FloorButton(Direction.ALL, "вызвать лифт")), SINGLE),
            new FloorPanel(Collections.singletonList(new FloorButton(Direction.UP, "вверх")), DOUBLE))),

    SECOND(2, createPanelsWithButtons()),
    THIRD(3, createPanelsWithButtons()),
    FOURTH(4, createPanelsWithButtons()),
    FIFTH(5, createPanelsWithButtons()),
    SIXTH(6, createPanelsWithButtons()),
    SEVENTH(7, createPanelsWithButtons()),
    EIGHTH(8, createPanelsWithButtons()),
    NINTH(9, createPanelsWithButtons()),

    TENTH(10, Arrays.asList(
            new FloorPanel(Collections.singletonList(new FloorButton(Direction.ALL, "вызвать лифт")), SINGLE),
            new FloorPanel(Collections.singletonList(new FloorButton(Direction.DOWN, "вниз")), DOUBLE)));

    @Getter
    private int number;

    @Getter
    private List<FloorPanel> floorPanels;

    @Getter
    private List<Person> waitingPersons = new ArrayList<>();

    Floor(int number, List<FloorPanel> floorPanels) {
        this.number = number;
        this.floorPanels = floorPanels;
    }

    private static List<FloorPanel> createPanelsWithButtons() {
        return Arrays.asList(
                new FloorPanel(Collections.singletonList(new FloorButton(Direction.ALL, "вызвать лифт")), SINGLE),
                new FloorPanel(Arrays.asList(
                        new FloorButton(Direction.UP, "вверх"),
                        new FloorButton(Direction.DOWN, "вниз")), DOUBLE));
    }

    public boolean isButtonActive(Direction direction) {
        for (FloorButton floorButton : getButtonsFromActivePanel()) {
            if (floorButton.getDirection() == direction) {
                return floorButton.isActive();
            }
        }

        return false;
    }

    public FloorButton activateButton(Direction direction) {
        for (FloorButton floorButton : getButtonsFromActivePanel()) {
            if (floorButton.getDirection() == direction) {
                floorButton.setActive(true);
                return floorButton;
            }
        }

        return null;
    }

    public void deactivateAllButtons() {
        for (FloorButton floorButton : getButtonsFromActivePanel()) {
            floorButton.setActive(false);
        }
    }

    public List<FloorButton> getButtonsFromActivePanel() {
        for (FloorPanel floorPanel : this.floorPanels) {
            if (floorPanel.isWork()) {
                return floorPanel.getFloorButtons();
            }
        }

        return Collections.emptyList();
    }

    public FloorPanel getActivePanel() {
        for (FloorPanel floorPanel : this.floorPanels) {
            if (floorPanel.isWork()) {
                return floorPanel;
            }
        }

        return null;
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
