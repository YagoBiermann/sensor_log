package com.server.sensor_log.domain.model.device.device_readings;

import com.server.sensor_log.domain.model.device.Timer;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.Period;

@Getter
@Setter
@TypeAlias("fan")
@Document(collection = "fans")
@SuperBuilder
@Slf4j
public class FanReading extends DataReading {
    @Builder.Default
    private Integer speed = 0;     // %
    @Builder.Default
    private Double voltage = 0.0;  // W
    private Timer timer;           // h
    @Builder.Default
    private Integer rpm = 0;

    public FanReading(String deviceId, Timer timer, Integer rpm, Double voltage, Integer speed) {
        super(deviceId);
        this.timer = timer;
        this.rpm = rpm;
        this.voltage = voltage;
        this.speed = speed;
    }

    public void setTimer(String duration, String daysActive) {
        if (this.timer == null) {
            this.timer = new Timer();
            log.info("Creating new timer for device: {}", this.getDeviceId());
        }
        this.timer.setTimer(Duration.parse(duration), Period.parse(daysActive));
    }

    public Boolean isActive(){
        return rpm > 0 && voltage > 0.5;
    }

    @Override
    public String toString() {
        String timerInfo = timer != null ? "{ status=%s }".formatted(timer.getStatus()) : "N/A";
        return "Fan{status=%s, speed=%d%%, voltage=%.2fW, timer=%s, rpm=%d}"
                .formatted(
                        isActive() ? "ON" : "OFF",
                        speed,
                        voltage,
                        timer,
                        rpm
                );
    }
}
