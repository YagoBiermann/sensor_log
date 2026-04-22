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
