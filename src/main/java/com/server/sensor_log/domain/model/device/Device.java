package com.server.sensor_log.domain.model.device;

import lombok.*;

import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@NoArgsConstructor()
@SuperBuilder
public abstract class Device {
    protected String id;
    protected String readingId;
    protected Instant readingTimestamp;
    protected Boolean active;
    protected String location;
    protected DeviceType type = DeviceType.GENERIC;
    protected String topic;

    protected Device(String id, String location, DeviceType type, String topic) {
        validate(id, location, topic);
        this.id = id;
        this.readingId = id + "_" + readingTimestamp;
        this.readingTimestamp = Instant.now();
        this.location = location;
        this.type = type;
        this.topic = topic;
    }

    private void validate(String id, String location, String topic) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id cannot be null or blank");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Location cannot be null or blank");
        }
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("Topic cannot be null or blank");
        }
    }
}