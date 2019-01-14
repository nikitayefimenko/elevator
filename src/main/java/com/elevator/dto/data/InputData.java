package com.elevator.dto.data;

import com.elevator.dto.PanelType;
import com.elevator.dto.Person;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputData {
    private PanelType activePanelType;
    private List<Person> waitingPersons;
}
