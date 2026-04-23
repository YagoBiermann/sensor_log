package com.server.sensor_log.documents;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TypeAlias("fan")
@AllArgsConstructor
@Document(collection = "fans")
public class Fan extends Sensor {
    private DeviceController speed;     // %
    private Double voltage;   // W
    private Timer timer;     // h
    private Integer rpm;

    public Fan() {
        this.name = "Fan";
        this.location = "Unknown";
        this.isActive = false;
        this.speed = new DeviceController();
        this.timer = new Timer();
        this.voltage = 0.0;
        this.rpm = 0;
        this.readingTimestamp = System.currentTimeMillis();
    }

    public void setSpeed(Integer speed) {
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
