package com.server.sensor_log.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "lights")
public class Light {

    @Id
    private String deviceId;
    private boolean status;
    private Integer intensity;
    private Integer voltage;
    private Integer timer;

    @Override
    public String toString() {
        return "Light(" + deviceId + "){"
                + "status=" + status
                + ", intensity=" + intensity + "%"
                + ", voltage=" + voltage + "w"
                + ", TimerOn=" + timer + "h"
                + ", timerOff=" + (timer - 24) + "h"
                + '}';
    }
}
