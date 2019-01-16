package com.elevator.dto;

import com.elevator.dto.buttons.Direction;
import com.elevator.dto.buttons.ElevatorButton;
import com.elevator.exception.ElevatorSystemException;
import com.elevator.exception.SystemError;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Elevator {
    public static final int HEIGHT_IN_MILLIMETERS = 4000;
    public static final int SPEED_IN_MILLIS = 1000;
    public static final int MAX_WEIGHT = 700;

    public Elevator(Floor currentFloor) {
        this.currentFloor = currentFloor;
    }

    @Getter
    @Setter
    private Direction movingDirection;

    @Getter
    @Setter
    private Floor currentFloor;

    @Getter
    private ElevatorButton[] elevatorButtons = ElevatorButton.values();

    @Getter
    private List<Person> insidePersons = new ArrayList<>();

    @Getter
    @Setter
    private boolean openedDoors;

    public void addInsidePerson(Person outsidePerson) {
        this.insidePersons.add(outsidePerson);
    }

    public void deactivateButton(int buttonValue) throws ElevatorSystemException {
        ElevatorButton elevatorButton = ElevatorButton.getButtonByValue(buttonValue);
        if (elevatorButton == null) {
            return;
        }

        if (elevatorButton.isActive()) {
            elevatorButton.setActive(false);
        }
    }

    public void pushStopButton() {
        if (!ElevatorButton.STOP.isActive()) {
            ElevatorButton.STOP.setActive(true);
            System.out.println("Лифт не может двигаться дальше, нажата кнопка STOP.");
        } else {
            ElevatorButton.STOP.setActive(false);
            System.out.println("Лифт может двигаться, кнопка STOP не активна.");
        }
    }

    public boolean isElevatorOverloaded() {
        return this.insidePersons.size() * Person.PERSON_WEIGHT > Elevator.MAX_WEIGHT - Person.PERSON_WEIGHT;
    }

    public static void sleepForAction(int sleepTimeout) {
        try {
            Thread.sleep(sleepTimeout);
        } catch (InterruptedException e) {
            throw new SystemError("Одна из систем лифта вышла из строя. Пожалуйста перезапустите лифт.");
        }
    }


}
