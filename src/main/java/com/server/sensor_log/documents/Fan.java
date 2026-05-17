package com.server.sensor_log.documents;

import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.Duration;
import java.time.Period;

@Data
@EqualsAndHashCode(callSuper = true)
@TypeAlias("fan")
@Document(collection = "fans")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Component
@Slf4j
@Validated
public class Fan extends Sensor {

    @Min(value = 0, message = "speed must be >= 0")
    @Max(value = 100, message = "speed must be <= 100")
    private Integer speed = 0;     // %
    private Double voltage = 0.0;  // W
    private Timer timer;           // h
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
        String timerInfo = timer != null ? "{ status=%s }".formatted(timer.getStatus()) : "N/A";
        return "Fan{name='%s', status=%s, speed=%d%%, voltage=%.2fW, timer=%s, rpm=%d}"
                .formatted(
                        getName(),
                        isActive() ? "ON" : "OFF",
                        speed,
                        voltage,
                        timer,
                        rpm
                );
    }
}
