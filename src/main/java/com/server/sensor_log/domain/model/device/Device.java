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
public class Device {
    private String deviceId;
    private List<String> readingIds;
    private Instant readingTimestamp;
    private Boolean active;
    private String location;
    private String subLocation;
    private DeviceType type = DeviceType.GENERIC;
    public Timer timer;

    public Device(String deviceId, List<String> readingIds, String location, String subLocation, Boolean active, DeviceType type, Timer timer) {
        validate(deviceId, location, subLocation);
        this.deviceId = deviceId;
        this.readingIds = readingIds;
        this.readingTimestamp = Instant.now();
        this.location = location;
        this.type = type;
        this.topic = topic;
        this.timer = timer;
        this.active = active;
        this.location = location;
        this.subLocation = subLocation;
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
        if (subLocation == null || subLocation.isBlank()) {
            throw new IllegalArgumentException("Topic cannot be null or blank");
        }
    }
}