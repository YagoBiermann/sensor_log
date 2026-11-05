package com.server.sensor_log.application.exceptions;

public class InvalidDeviceStatusException extends RuntimeException {

    public InvalidDeviceStatusException(String message, Object... args) {
        super(String.format(message, args));
    }
}