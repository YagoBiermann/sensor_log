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
    private final Double voltage = 0.0;

    public void setTimer(Integer hours, Integer minutes) {
        this.timer.setHours(hours);
        this.timer.setMinutes(minutes);
    }

    @Override
    public String toString() {
        return "Light(" + this.getId() + "){"
                + "status=" + (this.getActive() ? "ON" : "OFF")
                + ", intensity=" + this.getIntensity() + "%"
                + ", voltage=" + this.getVoltage() + "w"
                + ", Timer Status=" + (this.timer != null ? this.timer.getTimerStatus() : "N/A")
                + '}';
    }
}
