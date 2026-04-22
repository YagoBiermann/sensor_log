package com.server.sensor_log.documents;

import java.time.Instant;

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

    public Fan() {
        this.name = "Fan";
        this.location = "Unknown";
        this.isActive = false;
        this.speed = new SensorReading("speed", System.currentTimeMillis(), 0.0, "%");
        this.voltage = new SensorReading("voltage", System.currentTimeMillis(), 0.0, "watts");
        this.timer = new SensorReading("timer", System.currentTimeMillis(), 0.0, "hours");
        this.rpm = new SensorReading("rpm", System.currentTimeMillis(), 0.0, "rpm");
    }

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
