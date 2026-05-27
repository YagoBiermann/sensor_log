package com.server.sensor_log.domain.model.device.device_readings;

import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TypeAlias("temperature")
@Document(collection = "temperatures")
@Slf4j
public class TempReading extends DataReading {
    @Builder.Default
    private Integer temperature = 0;     // °C
    @Builder.Default
    private Integer humidity = 0;        // %
    @Builder.Default
    private Double ph = 0.0;             // pH
    @Builder.Default
    public Boolean active = false;

    public TempReading(String deviceId, Integer temp, Double ph, Integer humidity) {
        super(deviceId);
        validate(temp, humidity, ph);
        this.temperature = temp;
        this.ph = ph;
        this.humidity = humidity;
        this.active = isActive();
    }

    public Boolean isActive() {
        return temperature != 0 && humidity != 0 && ph != 0;
    }

    @Override
    public String toString() {
        return "Temperature(" + this.getDeviceId() + "){"
                + "status=" + (this.getActive() ? "ON" : "OFF")
                + ", temperature=" + this.getTemperature() + "°C"
                + ", humidity=" + this.getHumidity() + "%"
                + ", ph=" + this.getPh() + "pH"
                + '}';
    }

    private void validate(Integer temperature, Integer humidity, Double ph) {
        if (temperature == null) {
            throw new IllegalArgumentException("Temperature cannot be null");
        }
        if (humidity == null) {
            throw new IllegalArgumentException("Humidity cannot be null");
        }
        if (ph == null) {
            throw new IllegalArgumentException("pH cannot be null");
        }
    }
}
