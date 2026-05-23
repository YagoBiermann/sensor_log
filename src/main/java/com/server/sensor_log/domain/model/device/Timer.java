package com.server.sensor_log.domain.model.device;

import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;

@Data
@NoArgsConstructor
public class Timer {
    private String name = "Timer";
    private Boolean enabled = false;
    private Duration duration = Duration.ZERO;
    private final LocalDateTime currentTime = LocalDateTime.now();
    private Period daysActive = Period.ofDays(0);
    public String type = "TIMER";

    public Timer(String name, Boolean enabled, Duration duration, Period daysActive) {
        setTimer(duration, daysActive);
        this.name = name;
        this.enabled = enabled;
    }
    public String getStatus() {
        return enabled ? "ON" : "OFF";
    }
    public void setTimer(Duration duration, Period daysActive) {
        if(!isDurationValid(duration)) {
            throw new IllegalArgumentException("Duration cannot be negative or greater than 24 hours");
        }
        if(!isPeriodValid(daysActive)) {
            throw new IllegalArgumentException("Period cannot be negative or greater than 7 days");
        }
        this.duration = duration;
        this.daysActive = daysActive;
        if (duration.isPositive() && daysActive.getDays() >= 1) {
            this.enabled = true;
        }
    }

    public String ToString() {
        return display();
    }

    public String display() {

        LocalDateTime now = LocalDateTime.now();

        return """
                =========================
                TIMER: %s
                =========================
                Status: %s
                
                Current Time: %s
                Current Day: %s
                
                Cycle: %s
                Timer ON for %d hours and %d days a week
                
                =========================
                """.formatted(name, getStatus(), now.toLocalTime().withSecond(0).withNano(0), now.getDayOfWeek(), getCycle(), duration.toHours(), daysActive.getDays());
    }

    private String getCycle() {
        long onHours = duration.toHours();
        long offHours = 24 - onHours;

        return onHours + "/" + offHours;
    }

    private boolean isPeriodValid(Period period) {
        return period != null && !period.isNegative() && !(period.getDays() > 7);
    }

    private boolean isDurationValid(Duration duration) {
        return duration != null && !duration.isNegative() && duration.compareTo(Duration.ofHours(24)) <= 0;
    }
}
