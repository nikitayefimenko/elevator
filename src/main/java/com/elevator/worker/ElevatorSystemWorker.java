package com.elevator.worker;

import com.elevator.dto.Elevator;
import com.elevator.dto.ElevatorTask;
import com.elevator.dto.Floor;
import com.elevator.dto.Person;
import com.elevator.dto.buttons.Direction;
import com.elevator.dto.buttons.ElevatorButton;
import com.elevator.dto.buttons.FloorButton;
import com.elevator.exception.ElevatorSystemException;
import com.elevator.exception.SystemError;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class ElevatorSystemWorker {

    private Elevator elevator;

    private Queue<ElevatorTask> elevatorTaskQueue = new PriorityQueue<>();

    private static int priorityNumForQueue = 0;

    public ElevatorSystemWorker(Elevator elevator) {
        this.elevator = elevator;
    }

    public void runElevatorSystem() throws ElevatorSystemException {
        Floor floorToTakePersons = findFloorToTakePersons();
        while (floorToTakePersons != null) {
            moveTo(floorToTakePersons);
            takeAndReleasePersons();

            while (!this.elevatorTaskQueue.isEmpty()) {
                processTask(this.elevatorTaskQueue.poll());
            }
            floorToTakePersons = findFloorToTakePersons();
        }

        System.out.println("Двери закрываются..");
    }

    private Floor findFloorToTakePersons() {
        Floor minFloorWithActiveUpButton = null;
        Floor maxFloorWithActiveDownButton = null;
        for (Floor floor : Floor.values()) {
            List<FloorButton> floorButtons = floor.getFloorButtons();
            for (FloorButton floorButton : floorButtons) {
                if (floorButton.getDirection() == Direction.UP && floorButton.isActive()) {
                    if (minFloorWithActiveUpButton == null || minFloorWithActiveUpButton.getNumber() > floor.getNumber()) {
                        minFloorWithActiveUpButton = floor;
                    }
                } else if (floorButton.getDirection() == Direction.DOWN && floorButton.isActive()) {
                    if (maxFloorWithActiveDownButton == null || maxFloorWithActiveDownButton.getNumber() < floor.getNumber()) {
                        maxFloorWithActiveDownButton = floor;
                    }
                }
            }
        }

        if (minFloorWithActiveUpButton != null) {
            return minFloorWithActiveUpButton;
        } else if (maxFloorWithActiveDownButton != null) {
            return maxFloorWithActiveDownButton;
        } else {
            return null;
        }
    }

    private void processTask(ElevatorTask elevatorTask) throws ElevatorSystemException {
        Floor destinationFloor = elevatorTask.getDestinationFloor();
        Direction movingDirection = calcMovingDirection(this.elevator.getCurrentFloor().getNumber(), destinationFloor.getNumber());
        this.elevator.setMovingDirection(movingDirection);
        Floor nextFloor = this.elevator.getCurrentFloor();
        do {
            nextFloor = getNextFloor(nextFloor, movingDirection);
            if (isNeedToStopInNextFloor(nextFloor)) {
                moveTo(nextFloor);
                takeAndReleasePersons();
            }
        } while (destinationFloor != nextFloor);
    }

    private void moveTo(Floor destinationFloor) throws ElevatorSystemException {
        checkStopButton();
        int destinationFloorNumber = destinationFloor.getNumber();
        int movingTime = calcMovingTime(this.elevator.getCurrentFloor().getNumber(), destinationFloorNumber) * 1000;

        boolean messageSent = false;
        while (movingTime > 0) {
            checkStopButton();

            if (!messageSent) {
                closeDoors();
                System.out.println(String.format("Началось движение на %d этаж...\n", destinationFloorNumber));
                messageSent = true;
            }

            Elevator.sleepForAction(100);
            movingTime -= 100;

            if (movingTime % 4000 == 0) {
                System.out.println("...Лифт проехал один этаж\n");
            }
        }

        this.elevator.setCurrentFloor(destinationFloor);
        System.out.println("Лифт прибыл к пункту назначения. Этаж - " + destinationFloorNumber);
        this.elevator.deactivateButton(destinationFloorNumber);
        openDoors();
    }

    private void takeAndReleasePersons() {
        Floor floorToTakePersons = this.elevator.getCurrentFloor();
        floorToTakePersons.deactivateAllButtons();

        releasePersonsAndRemoveTasks();

        takePersonsAndAddTasks(floorToTakePersons.getWaitingPersons());
    }

    private void releasePersonsAndRemoveTasks() {
        this.elevator.getInsidePersons().removeIf(person -> {
            int finishFloor = person.getFinishFloor();
            if (finishFloor == this.elevator.getCurrentFloor().getNumber()) {
                System.out.println(String.format("%s прибыл к пункту назначения - %d этаж и вышел из лифта.", person.getName(), finishFloor));
                Elevator.sleepForAction(1000);
                return true;
            } else {
                return false;
            }
        });

        this.elevatorTaskQueue.removeIf(elevatorTask -> elevatorTask.getDestinationFloor().getNumber() == this.elevator.getCurrentFloor().getNumber());
    }

    private void takePersonsAndAddTasks(List<Person> waitingPersons) {
        this.elevator.addInsidePersons(waitingPersons);
        waitingPersons.forEach(person -> {
            System.out.println(person.getName() + " зашел в лифт");
            Elevator.sleepForAction(1000);
        });

        waitingPersons.forEach(person -> {
            this.elevatorTaskQueue.add(new ElevatorTask(Floor.getFloorByNumber(person.getFinishFloor()), ++priorityNumForQueue));
            try {
                this.elevator.activateButton(person.getFinishFloor());
            } catch (ElevatorSystemException e) {
                throw new SystemError(e.getMessage());
            }
            System.out.println(person.getName() + " нажал на кнопку " + person.getFinishFloor() + " внутри лифта");
            Elevator.sleepForAction(500);

        });

        waitingPersons.clear();
    }

    private void checkStopButton() throws ElevatorSystemException {
        boolean messageSent = false;
        while (ElevatorButton.STOP.isActive()) {
            if (!messageSent) {
                System.out.println("Лифт не может ехать. Ожидается повторное нажатие кнопки STOP...");
                messageSent = true;
            }
            Elevator.sleepForAction(100);
        }

        if (messageSent) {
            System.out.println("Лифт возобновил движение..");
        }
    }

    private void closeDoors() throws ElevatorSystemException {
        System.out.println("\nДвери лифта закрываются..");
        Elevator.sleepForAction(1000);
    }

    private void openDoors() throws ElevatorSystemException {
        System.out.println("Двери лифта открываются..");
        Elevator.sleepForAction(1000);
    }

    private Direction calcMovingDirection(int startFloor, int finalFloor) {
        int diff = startFloor - finalFloor;
        if (diff < 0) {
            return Direction.UP;
        } else {
            return Direction.DOWN;
        }
    }

    private Floor getNextFloor(Floor currentFloor, Direction direction) throws ElevatorSystemException {
        Floor nextFloor = null;
        if (direction == Direction.UP) {
            nextFloor = Floor.getFloorByNumber(currentFloor.getNumber() + 1);
        } else {
            nextFloor = Floor.getFloorByNumber(currentFloor.getNumber() - 1);
        }

        if (nextFloor == null) {
            throw new ElevatorSystemException("Сбой в системе движения лифта. Невозможно определить следующий по направлению этаж.");
        }

        return nextFloor;
    }

    private boolean isNeedToStopInNextFloor(Floor nextFloor) throws ElevatorSystemException {
        return isPersonsInsideToReleaseInNextFloor(nextFloor) | isWaitingPersonsOnNextFloor(nextFloor);
    }

    private boolean isWaitingPersonsOnNextFloor(Floor nextFloor) {
        List<FloorButton> nextFloorButtons = nextFloor.getFloorButtons();
        for (FloorButton floorButton : nextFloorButtons) {
            if (floorButton.getDirection() == this.elevator.getMovingDirection() && floorButton.isActive()) {
                StringBuilder waitingNamesBuilder = new StringBuilder("Ожидающие: ");
                for (Person person : nextFloor.getWaitingPersons()) {
                    waitingNamesBuilder.append(person.getName()).append(", ");
                }
                String names = waitingNamesBuilder.toString().substring(0, waitingNamesBuilder.length() - 2);
                System.out.println("\nНужно подобрать людей с " + nextFloor.getNumber() + " этажа. " + names);
                return true;
            }
        }

        return false;
    }

    private boolean isPersonsInsideToReleaseInNextFloor(Floor nextFloor) throws ElevatorSystemException {
        ElevatorButton nextFloorElevatorButton = ElevatorButton.getButtonByValue(nextFloor.getNumber());
        if (nextFloorElevatorButton.isActive()) {
            StringBuilder waitingNamesBuilder = new StringBuilder("Ожидающие высадки: ");
            for (Person person : elevator.getInsidePersons()) {
                if (person.getFinishFloor() == nextFloor.getNumber()) {
                    waitingNamesBuilder.append(person.getName()).append(", ");
                }
            }
            String names = waitingNamesBuilder.toString().substring(0, waitingNamesBuilder.length() - 2);
            System.out.println("\nНужно высадить людей на " + nextFloor.getNumber() + " этаже. " + names);
            return true;
        }
        return false;
    }


    private static int calcFloorDistance(int startFloor, int finalFloor) {
        return Math.abs(startFloor - finalFloor) * Elevator.HEIGHT_IN_MILLIMETERS / 1000;
    }

    public static int calcMovingTime(int distance) {
        return distance / (Elevator.SPEED_IN_MILLIS / 1000);
    }

    public static int calcMovingTime(int startFloor, int finalFloor) {
        return calcFloorDistance(startFloor, finalFloor) / (Elevator.SPEED_IN_MILLIS / 1000);
    }
}
