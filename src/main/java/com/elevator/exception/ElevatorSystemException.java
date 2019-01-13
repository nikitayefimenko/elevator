package com.elevator.exception;

public class ElevatorSystemException extends Exception {
    public ElevatorSystemException() {
    }

    public ElevatorSystemException(String message) {
        super(message);
    }

    public ElevatorSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElevatorSystemException(Throwable cause) {
        super(cause);
    }

    public ElevatorSystemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
