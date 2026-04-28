package com.server.sensor_log.documents;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.validation.annotation.Validated;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TypeAlias("temperature")
@Document(collection = "temperatures")
@Validated
public class Temperature extends Sensor {

    private Integer temperature = 0;     // °C
    private Integer humidity = 0;        // %
    private Double ph = 0.0;               // pH

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
