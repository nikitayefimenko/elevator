package com.elevator.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Person {

    public Person(String name, int startFloor, int finishFloor) {
        this.name = name;
        this.startFloor = startFloor;
        this.finishFloor = finishFloor;
    }

    private String name;
    private int startFloor;
    private int finishFloor;
    private Boolean stop;
    private int timeoutBeforePushStop;
    private int timeoutAfterPushStop;
}
