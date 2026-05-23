package com.server.sensor_log.documents.device;

import lombok.*;

import lombok.experimental.SuperBuilder;
@Data
@NoArgsConstructor()
@SuperBuilder
public abstract class Sensor {
    protected String id;
    protected String readingId;
    protected Long readingTimestamp;
    protected Boolean active;
    protected String location;

    protected Sensor(String id, Long readingTimestamp, String location) {
        validate(id, readingTimestamp, location);
        this.id = id;
        this.readingId = id + "_" + readingTimestamp;
        this.readingTimestamp = readingTimestamp;
        this.location = location;
    }

    private void validate(String id, Long readingTimestamp, String location) {
        if (readingTimestamp == null || readingTimestamp < 0) {
            throw new IllegalArgumentException("Invalid timestamp");
        }
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id cannot be null or blank");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Location cannot be null or blank");
        }
    }
}