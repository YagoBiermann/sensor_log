package com.server.sensor_log.documents;

import org.springframework.data.annotation.TypeAlias;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TypeAlias("fan")

public class Fan extends Sensor {

    private final SensorReading speed;     // %
    private final SensorReading voltage;   // W
    private final Timer timer;     // h
    private final SensorReading rpm;

    public Fan() {
        this.name = "Fan";
        this.location = "Unknown";
        this.isActive = false;
        this.speed = new SensorReading("speed", System.currentTimeMillis(), 0.0, "%");
        this.voltage = new SensorReading("voltage", System.currentTimeMillis(), 0.0, "watts");
        this.timer = new Timer();
        this.rpm = new SensorReading("rpm", System.currentTimeMillis(), 0.0, "rpm");
    }

    public void setSpeed(double speed) {
        if (speed < 0 || speed > 100) {
            throw new IllegalArgumentException("Speed must be between 0 and 100%");
        }
        this.speed.setValue(speed);
    }

    public void setTimer(Integer hours, Integer minutes) {
        this.timer.setTimer(hours, minutes);
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
