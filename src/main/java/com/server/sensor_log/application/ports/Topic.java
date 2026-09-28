package com.server.sensor_log.application.ports;

import lombok.Getter;

@Getter
public class Topic {
    public String deviceSerialNumber;
    public String actionType;

    public Topic(String deviceSerialNumber, String actionType) {
        this.deviceSerialNumber = deviceSerialNumber;
        this.actionType = actionType;
    }

    public String getTopic() {
        return "iot/" + deviceSerialNumber + "/" + actionType;
    }

    public static Topic parse(String topic) {
        String[] parts = topic.split("/");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid topic format");
        }
        if (!parts[0].equals("iot")) {
            throw new IllegalArgumentException("Invalid topic format: must start with 'iot'");
        }
        if (!parts[1].matches("^[a-zA-Z0-9]{12}$")) {
            throw new IllegalArgumentException("Invalid topic format: device serial number must be exactly 12 alphanumeric characters");
        }
        if (!parts[2].equals("data") && !parts[2].equals("command")) {
            throw new IllegalArgumentException("Invalid topic format: action type must be 'data' or 'command'");
        }

        return new Topic(parts[1], parts[2]);
    }
}
