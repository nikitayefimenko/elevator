package com.elevator.exception;

public class SystemError extends RuntimeException {
    public SystemError() {
    }

    public SystemError(String message) {
        super(message);
    }

    public SystemError(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemError(Throwable cause) {
        super(cause);
    }

    public SystemError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
