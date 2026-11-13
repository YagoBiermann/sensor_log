package com.server.sensor_log.application.exceptions;

public class RetryBeforeCooldownException extends RuntimeException {
    public RetryBeforeCooldownException(String message) {
        super(message);
    }
}
