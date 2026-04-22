package com.server.sensor_log.documents;

import org.springframework.data.annotation.TypeAlias;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TypeAlias("temperature")
public class Temperature extends Sensor {

    private SensorReading temperature;     // °C
    private SensorReading humidity;        // %
    private SensorReading ph;            // pH

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
