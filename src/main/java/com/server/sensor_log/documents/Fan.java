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

    public Fan(String id, String name, Long readingTimestamp, Boolean active, String location, Timer timer, Integer rpm, Double voltage, Integer speed) {
        super(id, name, readingTimestamp, active, location);
        this.timer = timer;
        this.rpm = rpm;
        this.voltage = voltage;
        this.speed = speed;
    }

    public void setTimer(String duration, String daysActive) {
        if (this.timer == null) {
            this.timer = new Timer();
            log.info("Creating new timer for device: {}", this.getName());
        }
        this.timer.setTimer(Duration.parse(duration), Period.parse(daysActive));
    }

    public Boolean isActive() {
        return this.rpm > 0;
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
