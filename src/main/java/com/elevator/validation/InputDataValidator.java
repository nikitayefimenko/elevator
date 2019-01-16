package com.elevator.validation;

import com.elevator.dto.Floor;
import com.elevator.dto.PanelType;
import com.elevator.dto.Person;
import com.elevator.dto.data.InputData;
import com.elevator.exception.ValidationException;
import com.elevator.json.JsonMapperConfigurer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.elevator.worker.ElevatorSystemWorker.calcMovingTime;

public class InputDataValidator {
    private static InputDataValidator ourInstance = new InputDataValidator();

    public static InputDataValidator gI() {
        return ourInstance;
    }

    private InputDataValidator() {
    }

    private static final String VALIDATION_ERROR_MESSAGE = "Ошибка! Вы ввели некорректный массив с данными в виде json. Повторите пожалуйста ввод, следуя примеру описанному выше.\n";

    public InputData validateAndGetInputData(String inputDataStr) throws ValidationException {
        if (inputDataStr.equals("test")) {
            return buildTestData();
        }

        try {
            ObjectMapper mapper = JsonMapperConfigurer.getObjectMapper();
            ObjectNode mainObjectNode = (ObjectNode) mapper.readTree(inputDataStr);
            InputData inputData = new InputData();
            inputData.setActivePanelType(validateAndGetPanelType(mainObjectNode));

            JsonNode inputArrayNode = mainObjectNode.get("persons");
            if (inputArrayNode == null || inputArrayNode.isNull() || !inputArrayNode.isArray()) {
                throw new ValidationException(VALIDATION_ERROR_MESSAGE);
            }

            List<Person> waitingPersons = new ArrayList<>();
            for (JsonNode person : inputArrayNode) {
                waitingPersons.add(mapper.treeToValue(person, Person.class));
            }

            validatePersons(waitingPersons);
            inputData.setWaitingPersons(waitingPersons);
            return inputData;
        } catch (IOException e) {
            throw new ValidationException(VALIDATION_ERROR_MESSAGE);
        }
    }

    private InputData buildTestData() {
        List<Person> testData = new ArrayList<>();

        testData.add(new Person("Mike", 2, 7));
        testData.add(new Person("Irina", 2, 6));
        testData.add(new Person("Helen", 2, 3));
        testData.add(new Person("Natalie", 2, 3));
        testData.add(new Person("Scott", 2, 10));
        testData.add(new Person("John", 2, 9));
        testData.add(new Person("Harry", 2, 3));
        testData.add(new Person("Ron", 2, 10));
        testData.add(new Person("Severus", 2, 5));
        testData.add(new Person("Albus", 2, 3));
        testData.add(new Person("Freddy", 2, 6));
        testData.add(new Person("Jason", 6, 4));
        testData.add(new Person("Lily", 4, 10));
        testData.add(new Person("Bruce", 3, 10));
        testData.add(new Person("Alex", 2, 10));
        testData.add(new Person("Marty", 6, 1));
        testData.add(new Person("Gloria", 7, 1));
        testData.add(new Person("Kovalski", 8, 1));

        Person vipPerson = new Person("Nikita", 5, 1);
        vipPerson.setVip(true);
        testData.add(vipPerson);

        testData.add(new Person("Artur", 5, 10));

        return new InputData(PanelType.SINGLE, testData);
    }

    private PanelType validateAndGetPanelType(ObjectNode mainObjectNode) throws ValidationException {
        JsonNode panelNode = mainObjectNode.get("panel");
        if (panelNode == null || panelNode.isNull()) {
            throw new ValidationException(VALIDATION_ERROR_MESSAGE + "Отсутствует название рабочей панели лифта - panel");
        }

        String panel = panelNode.asText().toUpperCase();
        if (panel.equals(PanelType.SINGLE.name())) {
            return PanelType.SINGLE;
        } else if (panel.equals(PanelType.DOUBLE.name())) {
            return PanelType.DOUBLE;
        } else {
            throw new ValidationException(VALIDATION_ERROR_MESSAGE + "Некорректное название рабочей панели лифта - panel. Необходимо single или double");
        }
    }

    private void validatePersons(List<Person> persons) throws ValidationException {
        if (persons.isEmpty()) {
            throw new ValidationException(VALIDATION_ERROR_MESSAGE + "Введенный массив пуст.");
        }

        for (Person person : persons) {
            checkPersonName(person.getName());

            checkFloorParameters(person);

            if (isStopPresent(person)) {
                checkStopParameters(person);
            }
        }
    }

    private void checkPersonName(String name) throws ValidationException {
        if (name == null) {
            throw new ValidationException(VALIDATION_ERROR_MESSAGE + "Вы ввели человека без имени.");
        }

        if (name.length() > 100) {
            throw new ValidationException(VALIDATION_ERROR_MESSAGE + "Имя человека не может быть более 100 символов.");
        }
    }

    private void checkFloorParameters(Person person) throws ValidationException {
        String name = person.getName();

        int startFloor = person.getStartFloor();
        int finalFloor = person.getFinishFloor();
        checkFloor(startFloor, "startFloor", name);
        checkFloor(finalFloor, "finishFloor", name);

        if (startFloor == finalFloor) {
            throw new ValidationException(VALIDATION_ERROR_MESSAGE + "Поля startFloor и finishFloor не могут быть равны.");
        }
    }

    private void checkFloor(int floor, String nameOfField, String personName) throws ValidationException {
        if (floor < 1 || floor > Floor.values().length) {
            throw new ValidationException(VALIDATION_ERROR_MESSAGE + String.format("Поле %s имеет не корректное значение %d у человека с именем %s. Требуется число от 1 до 4.", nameOfField, floor, personName));
        }
    }

    private void checkStopParameters(Person person) throws ValidationException {
        String name = person.getName();
        int timeoutBeforePushStop = person.getTimeoutBeforePushStop();
        int timeoutAfterPushStop = person.getTimeoutAfterPushStop();
        if (timeoutBeforePushStop >= calcMovingTime(person.getStartFloor(), person.getFinishFloor())) {
            throw new ValidationException(VALIDATION_ERROR_MESSAGE + String.format("Поле timeoutBeforePushStop слишком большое. Лифт довезет человека %s быстрее чем он нажмет кнопку. Пожалуйста, сделайте значение меньше.", name));
        }

        if (timeoutAfterPushStop > 60) {
            throw new ValidationException(VALIDATION_ERROR_MESSAGE + String.format("Поле timeoutAfterPushStop у человека %s не может быть больше 60 секунд. Пожалуйста, сделайте значение меньше.", name));
        }
    }

    private boolean isStopPresent(Person person) {
        return person.getStop() != null && person.getStop().equals(Boolean.TRUE);
    }
}
