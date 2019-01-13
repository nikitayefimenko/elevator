package com.elevator;


import com.elevator.job.ElevatorStartJob;

public class MainStartElevator {
    public static void main(String[] args) {
        ElevatorStartJob.gI().start();
    }

}
