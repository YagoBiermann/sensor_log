package com.server.sensor_log.documents;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "Sensors")
public class Sensor {

    @Id
    private String id;
    private String deviceId;
    private String sensorType;
    private long timestamp;
    private Integer temperature;
    private Integer humidity;
    private Double ph;
    private List<Light> light;
    private List<Fan> fan;

}
