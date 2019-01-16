
import com.elevator.dto.PanelType;
import com.elevator.dto.Person;
import com.elevator.dto.data.InputData;
import com.elevator.exception.ElevatorSystemException;
import com.elevator.job.ElevatorStartJob;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestElevatorSystem {

    @Test
    public void testElevatorStartJob() throws ElevatorSystemException {
        List<Person> testData = new ArrayList<>();
        testData.add(new Person("Nikita", 2, 10));
        testData.add(new Person("Mike", 3, 10));
        testData.add(new Person("Irina", 4, 1));
        testData.add(new Person("Alex", 3, 2));
        testData.add(new Person("Alex", 5, 1));
        testData.add(new Person("Dim", 5, 10));

        Person stopPerson1 = new Person("Scott", 3, 1);
        stopPerson1.setStop(true);
        stopPerson1.setTimeoutBeforePushStop(4);
        stopPerson1.setTimeoutAfterPushStop(6);
        testData.add(stopPerson1);

        Person stopPerson2 = new Person("Andrew", 2, 4);
        stopPerson2.setStop(true);
        stopPerson2.setTimeoutBeforePushStop(2);
        stopPerson2.setTimeoutAfterPushStop(10);
        testData.add(stopPerson2);

        testData.add(new Person("Severus", 5, 10));
        testData.add(new Person("Albus", 2, 3));
        testData.add(new Person("Freddy", 6, 10));
        testData.add(new Person("Jason", 6, 4));
        testData.add(new Person("Lily", 4, 10));
        testData.add(new Person("Bruce", 3, 10));
        testData.add(new Person("Alex", 2, 10));
        testData.add(new Person("Marty", 6, 1));
        testData.add(new Person("Gloria", 7, 1));

        Person vipPerson = new Person("Nikita", 5, 1);
        vipPerson.setVip(true);
        testData.add(vipPerson);

        Person vipPerson2 = new Person("Kovalski", 5, 10);
        vipPerson2.setVip(true);
        testData.add(vipPerson2);

        testData.add(new Person("Artur", 5, 10));

        InputData inputData = new InputData(PanelType.DOUBLE, testData);

        ElevatorStartJob.gI().pushButtonsAndRunSystem(inputData);
    }
}
