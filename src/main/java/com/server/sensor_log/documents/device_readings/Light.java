package com.server.sensor_log.documents;

import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.*;

import java.time.Duration;
import java.time.Period;

@Data
@EqualsAndHashCode(callSuper = true)
@TypeAlias("light")
@NoArgsConstructor
@Document(collection = "lights")
@SuperBuilder
@Slf4j
public class Light extends Sensor {
    @Builder.Default
    private Integer intensity = 0;

    private Timer timer;
    @Builder.Default
    private Double voltage = 0.0;
    @Builder.Default
    public String type = "LIGHT";

    public Light(String id, String name, Long readingTimestamp, Boolean active, String location, Timer timer, Double voltage, Integer intensity) {
        super(id, name, readingTimestamp, active, location);
        validate(voltage, intensity);
        this.timer = timer;
        this.voltage = voltage;
        this.intensity = intensity;
    }

    public void setTimer(String duration, String daysActive) {
        if (this.timer == null) {
            this.timer = new Timer();
            log.info("Creating new timer for device: {}", this.getName());
        }

        this.timer.setTimer(Duration.parse(duration), Period.parse(daysActive));
    }

    public Timer getTimer() {
        if (this.timer == null) {
            log.info("🟠 Timer not set to device: {}", this.getName());
        }

        return this.timer;
    }

    @Override
    public String toString() {
        String timerInfo = timer != null ? "status=" + timer.getStatus() : "N/A";
        String status = this.voltage > 0 ? "ON" : "OFF";

        return String.format(
                "Light(%s){ status=%s, intensity=%d%%, voltage=%sw, timer={ %s } }",
                getName(), status, intensity, voltage, timerInfo
        );
    }

    private void validate(Double voltage, Integer intensity) {
        if (intensity == null) {
            throw new IllegalArgumentException("Intensity cannot be null");
        }
        if (voltage == null) {
            throw new IllegalArgumentException("Intensity cannot be null");
        }
        if (intensity < 0 || intensity > 100) {
            throw new IllegalArgumentException(
                    "Intensity must be between 0 and 100"
            );
        }
        if (voltage < 0) {
            throw new IllegalArgumentException(
                    "Voltage must be positive"
            );
        }
    }
}
