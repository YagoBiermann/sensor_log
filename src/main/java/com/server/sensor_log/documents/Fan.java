package com.server.sensor_log.documents;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TypeAlias("fan")
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "fans")
public class Fan extends Sensor {

    private DeviceController speed;     // %
    private Double voltage = 0.0;             // W
    private Timer timer;                // h
    private Integer rpm = 0;

    public Fan(DeviceController speed, Timer timer) {
        this.speed = speed;
        this.timer = timer;
        this.setName("Fan");
    }

    public void setSpeed(Integer speed) {
        this.speed.setValue(speed);
    }

    public void setTimer(Integer hours, Integer minutes) {
        this.timer.setHours(hours);
        this.timer.setMinutes(minutes);
    }

    @Override
    public String toString() {
        return "Fan(" + this.getId() + "){"
                + "status=" + (this.getActive() ? "ON" : "OFF")
                + ", speed=" + this.getSpeed() + "%"
                + ", voltage=" + this.getVoltage() + "w"
                + ", timer=" + this.getTimer() + "h"
                + ", rpm=" + this.getRpm() + "rpm"
                + '}';
    }
}
