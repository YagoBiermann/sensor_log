package com.server.sensor_log.documents;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TypeAlias("light")
@AllArgsConstructor
@Document(collection = "lights")
public class Light extends Sensor {

    private DeviceController intensity;
    private Double voltage;
    private Timer timer;

    public Light() {
        this.name = "Light";
        this.location = "Unknown";
        this.isActive = false;
        this.intensity = new DeviceController();
        this.voltage = 0.0;
        this.timer = new Timer();
        this.readingTimestamp = System.currentTimeMillis();
    }

    public void setTimer(Integer hours, Integer minutes) {
        this.timer.setTimer(hours, minutes);
    }

    public void setIntensity(Integer intensity) {
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
