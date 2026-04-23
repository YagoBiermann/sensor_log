package com.server.sensor_log.documents;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public abstract class Sensor {
    @Id
    public String id;
    public String name;
    public boolean isActive;
    public String location;
}