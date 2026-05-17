package com.server.sensor_log.documents;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TypeAlias("light")
@Document(collection = "lights")
@RequiredArgsConstructor
@Component
@Validated
public class Light extends Sensor {

    @Min(value = 0, message = "Intensity must be >= 0")
    @Max(value = 100, message = "Intensity must be <= 100")
    private Integer intensity = 0;
    private Timer timer;
    @Positive
    private Double voltage = 0.0;

    public Light(String id, String name, Long readingTimestamp, Boolean active, String location, Timer timer, Double voltage, Integer intensity) {
        super(id, name, readingTimestamp, active, location);
        this.timer = timer;
        this.intensity = intensity;
        this.voltage = voltage;
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
}
