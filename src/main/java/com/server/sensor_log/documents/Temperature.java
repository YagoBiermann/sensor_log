package com.server.sensor_log.documents;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

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
public class Temperature extends Sensor {

    private Integer temperature = 0;     // °C
    private Integer humidity = 0;        // %
    private Double ph = 0.0;               // pH

    @Override
    public String toString() {
        return "Temperature(" + id + "){"
                + "status=" + (isActive ? "ON" : "OFF")
                + ", temperature=" + temperature + "°C"
                + ", humidity=" + humidity + "%"
                + ", ph=" + ph + "pH"
                + '}';
    }

}
