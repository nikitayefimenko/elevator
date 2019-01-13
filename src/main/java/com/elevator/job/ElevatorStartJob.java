package com.elevator.job;

import com.elevator.dto.Elevator;
import com.elevator.dto.Floor;
import com.elevator.dto.Person;
import com.elevator.dto.buttons.Direction;
import com.elevator.exception.ElevatorSystemException;
import com.elevator.exception.SystemError;
import com.elevator.exception.ValidationException;
import com.elevator.validation.ElevatorValidator;
import com.elevator.worker.ElevatorSystemWorker;

import java.util.List;
import java.util.Scanner;

public class ElevatorStartJob {
    private static ElevatorStartJob ourInstance = new ElevatorStartJob();

    public static ElevatorStartJob gI() {
        return ourInstance;
    }

    private ElevatorStartJob() {
    }

    public void start() {
        printStartInstruction();
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String inputData = getInputDataFromConsole(scanner);

                List<Person> waitingPersons = null;
                try {
                    waitingPersons = ElevatorValidator.gI().validateAndGetPersonsFromInputData(inputData.toString());
                } catch (ValidationException e) {
                    System.out.println(e.getMessage());
                    continue;
                }

                try {
                    runElevatorSystem(waitingPersons);
                } catch (ElevatorSystemException | SystemError e) {
                    System.out.println(e.getMessage());
                    break;
                }

                System.out.println("\nРабота лифта завершена. Желаете ввести новые данные? (да/нет)\n");
                String continueAnswer = scanner.nextLine();
                if (!continueAnswer.equals("да")) {
                    System.out.println("Спасибо что воспользовались нашими услугами. До свидания!");
                    break;
                }

                printStartInstruction();
            }
        }
    }

    private void runElevatorSystem(List<Person> waitingPersons) throws ElevatorSystemException {
        sendPersonsToFloorsAndPushButtons(waitingPersons);
        ElevatorSystemWorker elevatorSystemWorker = new ElevatorSystemWorker(new Elevator(Floor.FIRST));
        elevatorSystemWorker.runElevatorSystem();
    }

    private String getInputDataFromConsole(Scanner scanner) {
        StringBuilder inputData = new StringBuilder();
        while (scanner.hasNext()) {
            String line = scanner.nextLine().trim();
            if (line.endsWith("end")) {
                line = line.substring(0, line.length() - 3);
                inputData.append(line);
                break;
            }

            inputData.append(line);
        }

        return inputData.toString();
    }

    private void sendPersonsToFloorsAndPushButtons(List<Person> waitingPersons) {
        waitingPersons.forEach(person -> {
            int startFloorNumber = person.getStartFloor();
            Floor startFloor = Floor.getFloorByNumber(startFloorNumber);
            if (startFloor == null) {
                System.out.println("Невозможно определить текущий этаж человека " + person.getName());
            } else {
                startFloor.addPersonToFloor(person);
                if (isPersonMoveUp(startFloorNumber, person.getFinishFloor())) {
                    startFloor.activateButton(Direction.UP);
                } else {
                    startFloor.activateButton(Direction.DOWN);
                }
            }
        });
    }

    private boolean isPersonMoveUp(int startFloorNumber, int finishFloorNumber) {
        return startFloorNumber < finishFloorNumber;
    }

    private void printStartInstruction() {
        System.out.println("\nВведите массив людей (в формате json), ожидающих лифт, указав так-же их параметры, после окончания ввода данных вида json необходимо написать end.\n" +
                "Все параметры кроме stop, timeoutBeforePushStop, timeoutAfterPushStop - обязательные\n" +
                "timeoutBeforePushStop - время в секундах (целое число), которое отсчитывается после нажатия кнопки (finishFloor) человеком внутри лифта, по истечению данного времени человек нажимает кнопку STOP\n" +
                "timeoutAfterPushStop - время в секундах (целое число), которое отсчитывается после нажатия кнопки STOP, по истечению данного времени человек нажимает кнопку STOP для продолжения движения лифта\n" +
                "\n" +
                "Пример:\n" +
                "[\t\t{\n" +
                "\t\t\t\"name\": \"Никита\",\n" +
                "\t\t\t\"startFloor\": 1,\n" +
                "\t\t\t\"finishFloor\": 4\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"Олег\",\n" +
                "\t\t\t\"startFloor\": 3,\n" +
                "\t\t\t\"finishFloor\": 2,\n" +
                "\t\t\t\"stop\": false\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"Ирина\",\n" +
                "\t\t\t\"startFloor\": 4,\n" +
                "\t\t\t\"finishFloor\": 1,\n" +
                "\t\t\t\"stop\": true,\n" +
                "\t\t\t\"timeoutBeforePushStop\": 3,\n" +
                "\t\t\t\"timeoutAfterPushStop\": 1\n" +
                "\t\t}\n" +
                "]end\n" +
                "\n" +
                "Если желаете запустить тестовый пример из задания - введите test\n" +
                ">");
    }
}
