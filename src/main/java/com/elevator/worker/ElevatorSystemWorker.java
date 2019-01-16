package com.elevator.worker;

import com.elevator.dto.Elevator;
import com.elevator.dto.ElevatorTask;
import com.elevator.dto.Floor;
import com.elevator.dto.Person;
import com.elevator.dto.buttons.Direction;
import com.elevator.dto.buttons.ElevatorButton;
import com.elevator.dto.buttons.FloorButton;
import com.elevator.exception.ElevatorSystemException;

import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class ElevatorSystemWorker {

    private Elevator elevator;

    private Queue<ElevatorTask> elevatorTaskQueue = new PriorityQueue<>();

    private Queue<ElevatorTask> vipTasksQueue = new PriorityQueue<>();

    private static int priorityNumForQueue = 0;

    public ElevatorSystemWorker(Elevator elevator) {
        this.elevator = elevator;
    }

    public void runElevatorSystem() throws ElevatorSystemException {
        Floor floorToTakePersons = findFloorToTakePersons();
        while (floorToTakePersons != null) {
            moveTo(floorToTakePersons);
            takeAndReleasePersons();

            processVipTasks();

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
        Floor nearestFloorWithActiveSingleButton = null;
        for (Floor floor : Floor.values()) {
            for (FloorButton floorButton : floor.getButtonsFromActivePanel()) {
                if (floorButton.getDirection() == Direction.UP && floorButton.isActive()) {
                    if (minFloorWithActiveUpButton == null || minFloorWithActiveUpButton.getNumber() > floor.getNumber()) {
                        minFloorWithActiveUpButton = floor;
                    }
                } else if (floorButton.getDirection() == Direction.DOWN && floorButton.isActive()) {
                    if (maxFloorWithActiveDownButton == null || maxFloorWithActiveDownButton.getNumber() < floor.getNumber()) {
                        maxFloorWithActiveDownButton = floor;
                    }
                } else if (floorButton.getDirection() == Direction.ALL && floorButton.isActive()) {
                    if (nearestFloorWithActiveSingleButton == null || isThisFloorNearThenOtherFlor(floor, nearestFloorWithActiveSingleButton)) {
                        nearestFloorWithActiveSingleButton = floor;
                    }
                }
            }
        }

        if (minFloorWithActiveUpButton != null) {
            return minFloorWithActiveUpButton;
        } else if (maxFloorWithActiveDownButton != null) {
            return maxFloorWithActiveDownButton;
        } else if (nearestFloorWithActiveSingleButton != null) {
            return nearestFloorWithActiveSingleButton;
        } else {
            return null;
        }
    }

    private boolean isThisFloorNearThenOtherFlor(Floor thisFloor, Floor otherFlor) {
        int currentFloorNumber = this.elevator.getCurrentFloor().getNumber();
        return calcFloorDistance(currentFloorNumber, thisFloor.getNumber()) < calcFloorDistance(currentFloorNumber, otherFlor.getNumber());
    }

    private void processTask(ElevatorTask elevatorTask) throws ElevatorSystemException {
        Floor destinationFloor = elevatorTask.getDestinationFloor();
        System.out.println(String.format("\nЛифт принял команду из очереди - перемещение на %d этаж", destinationFloor.getNumber()));
        Direction movingDirection = calcMovingDirection(this.elevator.getCurrentFloor().getNumber(), destinationFloor.getNumber());
        this.elevator.setMovingDirection(movingDirection);
        Floor nextFloor = this.elevator.getCurrentFloor();
        do {
            processVipTasks();
            nextFloor = getNextFloor(nextFloor, movingDirection);
            if (isNeedToStopInNextFloor(nextFloor)) {
                moveTo(nextFloor);
                takeAndReleasePersons();
            }
        } while (destinationFloor != nextFloor);
    }

    private void processVipTasks() throws ElevatorSystemException {
        while (!this.vipTasksQueue.isEmpty()) {
            ElevatorTask vipTask = this.vipTasksQueue.poll();
            Floor destinationFloor = vipTask.getDestinationFloor();
            System.out.println(String.format("\nЛифт принял VIP команду - перемещение без остановок на %d этаж", destinationFloor.getNumber()));
            Direction movingDirection = calcMovingDirection(this.elevator.getCurrentFloor().getNumber(), destinationFloor.getNumber());
            this.elevator.setMovingDirection(movingDirection);
            moveTo(destinationFloor);
            takeAndReleasePersons();
        }
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
                System.out.println(String.format("Началось перемещение на %d этаж...\n", destinationFloorNumber));
                messageSent = true;
            }

            Elevator.sleepForAction(100);
            movingTime -= 100;

            if (movingTime % 4000 == 0) {
                System.out.println("...Лифт проехал один этаж\n");
            }
        }

        this.elevator.setCurrentFloor(destinationFloor);
        System.out.println("\nЛифт прибыл к пункту назначения. Этаж - " + destinationFloorNumber);
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
        Iterator<Person> waitingPersonIterator = waitingPersons.iterator();
        while (waitingPersonIterator.hasNext()) {
            Person person = waitingPersonIterator.next();
            ElevatorButton elevatorButton = ElevatorButton.getButtonByValue(person.getFinishFloor());
            if (elevatorButton == null) {
                System.out.println(person.getName() + " не может поехать на этаж " + person.getFinishFloor() + ". Данная кнопка лифта отсутствует");
                waitingPersonIterator.remove();
                continue;
            }

            if (!this.elevator.isElevatorOverloaded()) {
                this.elevator.addInsidePerson(person);
                System.out.println(person.getName() + " зашел в лифт");
                Elevator.sleepForAction(1000);

                addNewTaskAndPushButton(person, elevatorButton);
                waitingPersonIterator.remove();
            } else {
                System.out.println(String.format("\nВ лифте находится максимальное количество людей. %s не смог зайти внутрь и продолжил ожидать лифт.", person.getName()));
                person.callTheElevator(Floor.getFloorByNumber(person.getStartFloor()));
            }
        }
    }

    private void addNewTaskAndPushButton(Person person, ElevatorButton elevatorButton) {
        ElevatorTask elevatorTask = new ElevatorTask(Floor.getFloorByNumber(person.getFinishFloor()), ++priorityNumForQueue);
        if (person.getVip() == Boolean.TRUE) {
            System.out.println(person.getName() + " вставил VIP ключ и повернул его.");
            person.pushElevatorButton(elevatorButton);
            this.vipTasksQueue.add(elevatorTask);
            System.out.println(person.getName() + " достал VIP ключ.");
        } else {
            this.elevatorTaskQueue.add(elevatorTask);

            if (!elevatorButton.isActive()) {
                person.pushElevatorButton(elevatorButton);
            } else {
                System.out.println(person.getName() + " поедет на " + person.getFinishFloor() + ". Данная кнопка лифта уже нажата.");
            }
        }

        if (person.getStop() == Boolean.TRUE) {
            pushStopButtonAsync(person);
        }
    }

    private void pushStopButtonAsync(Person person) {
        CompletableFuture.runAsync(() -> {
            Elevator.sleepForAction(person.getTimeoutBeforePushStop() * 1000);
            System.out.println("\n" + person.getName() + " нажимает кнопку STOP.\n");
            elevator.pushStopButton();

            Elevator.sleepForAction(person.getTimeoutAfterPushStop() * 1000);
            System.out.println("\n" + person.getName() + " нажимает кнопку STOP.\n");
            elevator.pushStopButton();
        });
    }

    private void checkStopButton() throws ElevatorSystemException {
        boolean messageSent = false;
        while (ElevatorButton.STOP.isActive()) {
            if (!messageSent) {
                System.out.println("Ожидается повторное нажатие кнопки STOP...");
                messageSent = true;
            }
            Elevator.sleepForAction(100);
        }

        if (messageSent) {
            System.out.println("\nЛифт возобновил движение..");
        }
    }

    private void closeDoors() {
        if (this.elevator.isOpenedDoors()) {
            System.out.println("\nДвери лифта закрываются..");
            this.elevator.setOpenedDoors(false);
            Elevator.sleepForAction(1000);
        }
    }

    private void openDoors() {
        if (!this.elevator.isOpenedDoors()) {
            System.out.println("Двери лифта открываются..");
            this.elevator.setOpenedDoors(true);
            Elevator.sleepForAction(1000);
        }
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
        List<FloorButton> nextFloorButtons = nextFloor.getButtonsFromActivePanel();
        for (FloorButton floorButton : nextFloorButtons) {
            Direction buttonDirection = floorButton.getDirection();
            if ((buttonDirection == this.elevator.getMovingDirection() || buttonDirection == Direction.ALL) && floorButton.isActive()) {
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
        if (nextFloorElevatorButton == null) {
            return false;
        }

        if (nextFloorElevatorButton.isActive()) {
            StringBuilder waitingNamesBuilder = new StringBuilder("Ожидающие высадки: ");
            for (Person person : this.elevator.getInsidePersons()) {
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
