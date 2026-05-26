package com.server.sensor_log.domain.model.device.device_readings;

import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@TypeAlias("light")
@Document(collection = "lights")
@SuperBuilder
@Slf4j
public class LightReading extends DataReading{
    @Builder.Default
    private Integer intensity = 0;
    @Builder.Default
    private Double voltage = 0.0;

    public LightReading(String deviceId, Double voltage, Integer intensity) {
        super(deviceId);
        validate(voltage, intensity);
        this.voltage = voltage;
        this.intensity = intensity;
    }

    public Boolean isActive() {
        return voltage > 0.5 && intensity > 0;
    }

    @Override
    public String toString() {
        String status = this.voltage > 0 ? "ON" : "OFF";

        return String.format(
                "Light(%s){ status=%s, intensity=%d%%, voltage=%sw }",
                getDeviceId(), status, intensity, voltage
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
