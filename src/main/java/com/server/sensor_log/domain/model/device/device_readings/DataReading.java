package com.server.sensor_log.domain.model.device.device_readings;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@SuperBuilder
@Getter
public abstract class DataReading {
    protected String deviceId;
    protected String readingId;
    protected Instant timestamp;

    public DataReading(String deviceId) {
        this.deviceId = deviceId;
        this.timestamp = Instant.now();
        this.readingId = deviceId + "_" + timestamp.toEpochMilli();
    }
}
