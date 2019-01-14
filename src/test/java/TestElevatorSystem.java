
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
        testData.add(new Person("Nikita", 1, 4));
        testData.add(new Person("Mike", 4, 1));
        testData.add(new Person("Irina", 1, 2));
        testData.add(new Person("Alex", 3, 2));
        testData.add(new Person("Alex", 3, 4));
        testData.add(new Person("Dim", 2, 1));

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

        InputData inputData = new InputData(PanelType.SINGLE, testData);

        ElevatorStartJob.gI().pushButtonsAndRunSystem(inputData);
    }
}
