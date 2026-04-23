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

    private SensorReading temperature;     // °C
    private SensorReading humidity;        // %
    private SensorReading ph;            // pH

    public Temperature() {
        this.name = "Temperature";
        this.location = "Unknown";
        this.isActive = false;
        this.temperature = new SensorReading("temperature", System.currentTimeMillis(), 0.0, "°C");
        this.humidity = new SensorReading("humidity", System.currentTimeMillis(), 0.0, "%");
        this.ph = new SensorReading("ph", System.currentTimeMillis(), 0.0, "pH");
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
