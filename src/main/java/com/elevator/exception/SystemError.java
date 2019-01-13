package com.elevator.exception;

public class SystemError extends RuntimeException {
    public SystemError(String message) {
        super(message);
    }
}
