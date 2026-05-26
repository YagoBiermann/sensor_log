package com.server.sensor_log.domain.model.device.device_readings;

import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.DeviceType;
import com.server.sensor_log.domain.model.device.Timer;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@TypeAlias("temperature")
@Document(collection = "temperatures")
@Slf4j
public class TempReading extends Device {
    @Builder.Default
    private Integer temperature = 0;     // °C
    @Builder.Default
    private Integer humidity = 0;        // %
    @Builder.Default
    private Double ph = 0.0;             // pH
    @Builder.Default
    public Boolean active = isActive();

    public TempReading(String id, String location, String topic, Integer temp, Double ph, Integer humidity) {
        super(id, location, DeviceType.TEMP, topic);
        validate(temp, humidity, ph);
        temperature = temp;
        this.ph = ph;
        this.humidity = humidity;
        this.active = isActive();
    }

    public Boolean isActive() {
        return temperature != 0 && humidity != 0 && ph != 0;
    }

    @Override
    public String toString() {
        return "Temperature(" + this.getId() + "){"
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
