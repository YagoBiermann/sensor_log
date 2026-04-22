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
    private Timer timer;

    public Light() {
        this.name = "Light";
        this.location = "Unknown";
        this.isActive = false;
        this.intensity = new SensorReading("intensity", System.currentTimeMillis(), 0.0, "%");
        this.voltage = new SensorReading("voltage", System.currentTimeMillis(), 0.0, "watts");
        this.timer = new Timer();
    }

    public void setTimer(Integer hours, Integer minutes) {
        this.timer.setTimer(hours, minutes);
    }

    public void setIntensity(double intensity) {
        if (intensity < 0 || intensity > 100) {
            throw new IllegalArgumentException("Intensity must be between 0 and 100%");
        }
        this.intensity.setValue(intensity);
    }

    @Override
    public String toString() {
        return "Light(" + id + "){"
                + "status=" + (isActive ? "ON" : "OFF")
                + ", intensity=" + intensity + "%"
                + ", voltage=" + voltage + "w"
                + ", Timer Status=" + timer.getTimerStatus()
                + '}';
    }
}
