package com.server.sensor_log.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "sensors")
public abstract class Sensor {
    @Id
    public String id;
    public String name;
    public boolean isActive;
    public String location;
}