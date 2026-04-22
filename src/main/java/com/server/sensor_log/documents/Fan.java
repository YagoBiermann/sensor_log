package com.server.sensor_log.documents;

import lombok.Data;

@Data
public class Fan {
    private String deviceId;
    private boolean status;
    private Integer speed;
    private Integer voltage;
    private Integer timer;
    private Integer rpm;

    @Override
    public String toString() {
        return "Fan(" + deviceId + "){"
                + "status=" + status
                + ", speed=" + speed + "%"
                + ", voltage=" + voltage + "w"
                + ", timer=" + timer + "h"
                + ", rpm=" + rpm + "rpm"
                + '}';
    }
}
