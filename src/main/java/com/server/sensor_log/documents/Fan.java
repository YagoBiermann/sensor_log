package com.server.sensor_log.documents;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Validated
public class Fan extends Sensor {

    @Min(value = 0, message = "speed must be >= 0")
    @Max(value = 100, message = "speed must be <= 100")
    private Integer speed = 0;     // %
    private Double voltage = 0.0;             // W
    private Timer timer;                // h
    private Integer rpm = 0;

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
