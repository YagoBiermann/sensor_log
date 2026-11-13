package com.server.sensor_log.application.exceptions;

public class MaxAttemptsFailedException extends RuntimeException {
    public MaxAttemptsFailedException(String message) {
        super(message);
    }
}
