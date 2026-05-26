package com.server.sensor_log.domain.model.device;

import lombok.*;

import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.List;

@Data
@NoArgsConstructor()
@SuperBuilder
@Slf4j
public abstract class Device {
    protected String id;
    protected List<String> readingId;
    protected Instant readingTimestamp;
    protected Boolean active;
    protected String location;
    protected DeviceType type = DeviceType.GENERIC;
    protected String topic;
    public Timer timer;

    protected Device(String id, List<String> readingIds, String location, DeviceType type, String topic, Timer timer) {
        validate(id, location, topic);
        this.id = id;
        this.readingId = readingIds;
        this.readingTimestamp = Instant.now();
        this.location = location;
        this.type = type;
        this.topic = topic;
        this.timer = timer;
    }

    public void setTimer(String duration, String daysActive) {
        if (this.timer == null) {
            this.timer = new Timer();
            log.info("Creating new timer for device");
        }

        this.timer.setTimer(Duration.parse(duration), Period.parse(daysActive));
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