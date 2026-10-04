package com.server.sensor_log.application.exceptions;

public class DeviceAlreadyExistsException extends RuntimeException {

    public DeviceAlreadyExistsException(String message) {
        super(message);
    }
}