package com.server.sensor_log.documents;

import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@TypeAlias("temperature")
@Document(collection = "temperatures")
@Slf4j
public class Temperature extends Sensor {
    @Builder.Default
    @NonNull
    private Integer temperature = 0;     // °C
    @Builder.Default
    @NonNull
    private Integer humidity = 0;        // %
    @Builder.Default
    @NonNull
    private Double ph = 0.0;             // pH

    @Override
    public String toString() {
        return "Temperature(" + this.getId() + "){"
                + "status=" + (this.getActive() ? "ON" : "OFF")
                + ", temperature=" + this.getTemperature() + "°C"
                + ", humidity=" + this.getHumidity() + "%"
                + ", ph=" + this.getPh() + "pH"
                + '}';
    }
}
