package com.server.sensor_log.documents;

import lombok.*;

import lombok.experimental.SuperBuilder;
@Data
@NoArgsConstructor()
@SuperBuilder
public abstract class Sensor {
    protected String id;
    protected String name;
    protected Long readingTimestamp;
    protected Boolean active;
    protected String location;

    protected Sensor(String id, String name, Long readingTimestamp, Boolean active, String location) {
        validate(id, name, readingTimestamp, active, location);
        this.id = id;
        this.name = name;
        this.readingTimestamp = readingTimestamp;
        this.active = active;
        this.location = location;
    }

    private void validate(String id, String name, Long readingTimestamp, Boolean active, String location) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (readingTimestamp == null || readingTimestamp < 0) {
            throw new IllegalArgumentException("Invalid timestamp");
        }
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id cannot be null or blank");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Location cannot be null or blank");
        }
        if (active == null) {
            throw new IllegalArgumentException("Active cannot be null");
        }
    }
}