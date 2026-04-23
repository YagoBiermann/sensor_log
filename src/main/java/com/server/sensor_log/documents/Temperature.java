package com.server.sensor_log.documents;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TypeAlias("temperature")
@Document(collection = "temperatures")
public class Temperature extends Sensor {

    private Integer temperature;     // °C
    private Integer humidity;        // %
    private Double ph;               // pH

    public Temperature() {
        this.name = "Temperature";
        this.location = "Unknown";
        this.readingTimestamp = System.currentTimeMillis();
        this.isActive = false;
        this.temperature = 0;
        this.humidity = 0;
        this.ph = 0.0;
    }

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
