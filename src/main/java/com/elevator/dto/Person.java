package com.elevator.dto;

import com.elevator.dto.buttons.Direction;
import com.elevator.dto.buttons.ElevatorButton;
import com.elevator.dto.buttons.FloorButton;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Person {

    public static final int PERSON_WEIGHT = 70;

    private String name;
    private int startFloor;
    private int finishFloor;
    private Boolean stop;
    private int timeoutBeforePushStop;
    private int timeoutAfterPushStop;
    private Boolean vip;

    public Person(String name, int startFloor, int finishFloor) {
        this.name = name;
        this.startFloor = startFloor;
        this.finishFloor = finishFloor;
    }

    public void pushElevatorButton(ElevatorButton elevatorButton) {
        elevatorButton.setActive(true);
        System.out.println(this.getName() + " нажал на кнопку \"" + elevatorButton.getName() + "\" внутри лифта");
        Elevator.sleepForAction(500);
    }

    public void callTheElevator(Floor startFloor) {
        if (startFloor == null) {
            System.out.println(this.name + "Находится на неизвестном этаже, невозможно вызвать лифт.");
            return;
        }

        FloorPanel floorPanel = startFloor.getActivePanel();
        if (floorPanel == null) {
            System.out.println(this.name + " не смог определить активную панель.");
            return;
        }

        switch (floorPanel.getPanelType()) {
            case SINGLE: {
                pushFloorButtonByPerson(Direction.ALL, startFloor);
                break;
            }
            case DOUBLE: {
                if (isPersonMoveUp(this.startFloor, this.finishFloor)) {
                    pushFloorButtonByPerson(Direction.UP, startFloor);
                } else {
                    pushFloorButtonByPerson(Direction.DOWN, startFloor);
                }
            }
        }
    }

    private void pushFloorButtonByPerson(Direction direction, Floor startFloor) {
        if (!startFloor.isButtonActive(direction)) {
            FloorButton button = startFloor.activateButton(direction);
            if (button == null) {
                System.out.println(String.format("%s не смог нажать свою кнопку на этаже %d", this.name, this.startFloor));
                return;
            }

            System.out.println(String.format("%s нажал кнопку \"%s\" на этаже %d\n", this.name, button.getName(), this.startFloor));
        }
    }

    private boolean isPersonMoveUp(int startFloorNumber, int finishFloorNumber) {
        return startFloorNumber < finishFloorNumber;
    }
}
