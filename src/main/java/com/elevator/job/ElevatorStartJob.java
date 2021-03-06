package com.elevator.job;

import com.elevator.dto.*;
import com.elevator.dto.data.InputData;
import com.elevator.exception.ElevatorSystemException;
import com.elevator.exception.SystemError;
import com.elevator.exception.ValidationException;
import com.elevator.validation.InputDataValidator;
import com.elevator.worker.ElevatorSystemWorker;

import java.nio.charset.StandardCharsets;
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
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name())) {
            while (true) {
                String inputDataStr = getInputDataFromConsole(scanner);

                InputData inputData = null;
                try {
                    inputData = InputDataValidator.gI().validateAndGetInputData(inputDataStr);
                } catch (ValidationException e) {
                    System.out.println(e.getMessage());
                    continue;
                }

                try {
                    pushButtonsAndRunSystem(inputData);
                } catch (ElevatorSystemException | SystemError e) {
                    System.out.println(e.getMessage());
                    break;
                }

                System.out.println("\nРабота лифта завершена. Желаете ввести новые данные? (yes/no)\n");
                String continueAnswer = scanner.nextLine();
                if (!continueAnswer.equals("yes")) {
                    System.out.println("Спасибо что воспользовались нашими услугами. До свидания!");
                    break;
                }

                printStartInstruction();
            }
        }
    }

    public void pushButtonsAndRunSystem(InputData inputData) throws ElevatorSystemException {
        sendPersonsToFloorsAndCallElevator(inputData);
        ElevatorSystemWorker elevatorSystemWorker = new ElevatorSystemWorker(new Elevator(Floor.FIRST));
        elevatorSystemWorker.runElevatorSystem();
    }

    private String getInputDataFromConsole(Scanner scanner) {
        StringBuilder inputData = new StringBuilder();
        while (scanner.hasNext()) {
            String line = scanner.nextLine().trim();
            if (line.equals("test")) {
                return line;
            }

            if (line.endsWith("end")) {
                line = line.substring(0, line.length() - 3);
                inputData.append(line);
                break;
            }

            inputData.append(line);
        }

        return inputData.toString();
    }

    private void sendPersonsToFloorsAndCallElevator(InputData inputData) {
        PanelType activePanelType = inputData.getActivePanelType();
        activateFloorPanel(activePanelType);

        inputData.getWaitingPersons().forEach(person -> {
            Floor startFloor = Floor.getFloorByNumber(person.getStartFloor());
            if (startFloor == null) {
                System.out.println("Невозможно определить текущий этаж человека " + person.getName());
            } else {
                startFloor.addPersonToFloor(person);
                System.out.println(String.format("%s ожидает лифт на %d этаже, хочет попасть на %d этаж", person.getName(), person.getStartFloor(), person.getFinishFloor()));
                person.callTheElevator(startFloor);
            }
        });
    }

    private void activateFloorPanel(PanelType activePanelType) {
        for (Floor floor : Floor.values()) {
            for (FloorPanel floorPanel : floor.getFloorPanels()) {
                if (floorPanel.getPanelType() == activePanelType) {
                    floorPanel.setWork(true);
                }
            }
        }
    }

    private void printStartInstruction() {
        System.out.println("\nВведите массив людей (в формате json), ожидающих лифт, указав так-же их параметры, после окончания ввода данных вида json необходимо написать end.\n" +
                "Все параметры кроме vip, stop, timeoutBeforePushStop, timeoutAfterPushStop - обязательные\n" +
                "timeoutBeforePushStop - время в секундах (целое число, по умолчанию 0), которое отсчитывается после нажатия кнопки (finishFloor) человеком внутри лифта, по истечению данного времени человек нажимает кнопку STOP\n" +
                "timeoutAfterPushStop - время в секундах (целое число, по умолчанию 0), которое отсчитывается после нажатия кнопки STOP, по истечению данного времени человек нажимает кнопку STOP для продолжения движения лифта\n" +
                "Если в параметры startFloor, finishFloor, timeoutBeforePushStop, timeoutAfterPushStop будет введено не целое значение - дробная часть числа будет опущена\n" +
                "Параметр panel может быть только со строковыми значениями single и double\n" +
                "\n" +
                "Пример:\n" +
                "{\n" +
                "\t\"panel\": \"single\",\n" +
                "\t\"persons\": [{\n" +
                "\t\t\t\"name\": \"Nikita\",\n" +
                "\t\t\t\"startFloor\": 1,\n" +
                "\t\t\t\"finishFloor\": 4\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"Mike\",\n" +
                "\t\t\t\"startFloor\": 3,\n" +
                "\t\t\t\"finishFloor\": 2,\n" +
                "\t\t\t\"stop\": false,\n" +
                "\t\t\t\"vip\": true\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"Irina\",\n" +
                "\t\t\t\"startFloor\": 4,\n" +
                "\t\t\t\"finishFloor\": 1,\n" +
                "\t\t\t\"stop\": true,\n" +
                "\t\t\t\"timeoutBeforePushStop\": 3,\n" +
                "\t\t\t\"timeoutAfterPushStop\": 1\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}end\n" +
                "Если желаете запустить тестовый пример - введите test\n" +
                ">");
    }
}
