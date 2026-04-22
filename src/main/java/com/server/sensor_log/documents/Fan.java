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

    public Fan() {
        this.name = "Fan";
        this.location = "Unknown";
        this.isActive = false;
        this.speed = new SensorReading("speed", System.currentTimeMillis(), 0.0, "%");
        this.voltage = new SensorReading("voltage", System.currentTimeMillis(), 0.0, "watts");
        this.timer = new SensorReading("timer", System.currentTimeMillis(), 0.0, "hours");
        this.rpm = new SensorReading("rpm", System.currentTimeMillis(), 0.0, "rpm");
    }

    public void setTimer(double hours) {
        if (hours < 0 || hours > 24) {
            throw new IllegalArgumentException("Timer value cannot be negative or exceed 24 hours");
        }

        this.timer.setValue(hours);
    }

    public void setSpeed(double speed) {
        if (speed < 0 || speed > 100) {
            throw new IllegalArgumentException("Speed must be between 0 and 100%");
        }
        this.speed.setValue(speed);
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
