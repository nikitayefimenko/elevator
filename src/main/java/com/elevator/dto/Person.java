package com.elevator.dto;

import lombok.Data;

@Data
public class Person {
    private String name;
    private int startFloor;
    private int finishFloor;
    private Boolean stop;
    private int timeoutBeforePushStop;
    private int timeoutAfterPushStop;
}
