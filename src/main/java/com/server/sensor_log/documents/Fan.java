package com.server.sensor_log.documents;

import org.springframework.data.annotation.TypeAlias;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TypeAlias("fan")

public class Fan extends Sensor {

    private SensorReading speed;     // %
    private SensorReading voltage;   // W
    private SensorReading timer;     // h
    private SensorReading rpm;

    @Override
    public String toString() {
        return "Fan(" + id + "){"
                + "status=" + (isActive ? "ON" : "OFF")
                + ", speed=" + speed + "%"
                + ", voltage=" + voltage + "w"
                + ", timer=" + timer + "h"
                + ", rpm=" + rpm + "rpm"
                + '}';
    }
}
