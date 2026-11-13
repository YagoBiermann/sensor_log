package com.server.sensor_log.application.exceptions;

public class InvalidCodeException extends RuntimeException
{
    public InvalidCodeException(String message) {
        super(message);
    }

}
