package com.elevator.validation;

import com.elevator.dto.Person;
import com.elevator.exception.ValidationException;
import com.elevator.json.JsonMapperConfigurer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.elevator.worker.ElevatorSystemWorker.calcMovingTime;

public class ElevatorValidator {
    private static ElevatorValidator ourInstance = new ElevatorValidator();

    public static ElevatorValidator gI() {
        return ourInstance;
    }

    private ElevatorValidator() {
    }

    private static final String VALIDATION_ERROR_MESSAGE = "Ошибка! Вы ввели некорректный массив с данными в виде json. Повторите пожалуйста ввод, следуя примеру описанному выше.\n";

    public List<Person> validateAndGetPersonsFromInputData(String inputData) throws ValidationException {
        if (inputData.equals("test")) {
            return buildTestData();
        }

        try {
            ObjectMapper mapper = JsonMapperConfigurer.getObjectMapper();
            JsonNode inputArrayNode = mapper.readTree(inputData);
            if (!inputArrayNode.isArray()) {
                throw new ValidationException(VALIDATION_ERROR_MESSAGE);
            }

            List<Person> waitingPersons = new ArrayList<>();
            for (JsonNode person : inputArrayNode) {
                waitingPersons.add(mapper.treeToValue(person, Person.class));
            }

            validatePersons(waitingPersons);
            return waitingPersons;
        } catch (IOException e) {
            throw new ValidationException(VALIDATION_ERROR_MESSAGE);
        }
    }

    private List<Person> buildTestData() {
        List<Person> testData = new ArrayList<>();
        testData.add(new Person("Никита", 1, 4));
        testData.add(new Person("Олег", 3, 2));
        testData.add(new Person("Ирина", 4, 1));

        return testData;
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
        if (floor < 1 || floor > 4) {
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
