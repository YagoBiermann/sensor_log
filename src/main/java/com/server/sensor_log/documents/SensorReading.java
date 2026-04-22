package com.server.sensor_log.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SensorReading {
    private String name;
    private long timestamp;
    private Double value;
    private String unit;

    @Override
    public String toString() {
        return "SensorReading{"
                + "name='" + name + '\''
                + ", timestamp=" + timestamp
                + ", value=" + value
                + ", unit='" + unit + '\''
                + '}';
    }
}
