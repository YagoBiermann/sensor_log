package com.server.sensor_log.documents;

import org.springframework.data.annotation.TypeAlias;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TypeAlias("light")
public class Light extends Sensor {

    private SensorReading intensity;
    private SensorReading voltage;
    private SensorReading timer;

    public Light() {
        this.name = "Light";
        this.location = "Unknown";
        this.isActive = false;
        this.intensity = new SensorReading("intensity", System.currentTimeMillis(), 0.0, "%");
        this.voltage = new SensorReading("voltage", System.currentTimeMillis(), 0.0, "watts");
        this.timer = new SensorReading("timer", System.currentTimeMillis(), 0.0, "hours");
    }

    @Override
    public String toString() {
        return "Light(" + id + "){"
                + "status=" + (isActive ? "ON" : "OFF")
                + ", intensity=" + intensity + "%"
                + ", voltage=" + voltage + "w"
                + ", TimerOn=" + timer.getValue() + "h"
                + ", timerOff=" + (timer.getValue() - 24) + "h"
                + '}';
    }
}
