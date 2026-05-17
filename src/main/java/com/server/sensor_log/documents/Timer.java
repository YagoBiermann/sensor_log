package com.server.sensor_log.documents;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.DurationDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Valid
public class Timer {

    @NotBlank(message = "Timer name must not be blank")
    private String name = "Timer";
    @NotNull
    private Boolean enabled = false;
    @NotNull
    private Duration duration = Duration.ZERO;
    @NotNull
    private final LocalDateTime currentTime = LocalDateTime.now();
    @NotNull
    private Period daysActive = Period.ofDays(0);

    @AssertTrue(message = "Days should be between 0 and 7")
    public boolean isPeriodValid() {
        return daysActive != null && !daysActive.isNegative() && !(daysActive.getDays() > 7);
    }

    @AssertTrue(message = "Duration must be between 0 and 24 hours")
    public boolean isDurationValid() {
        return duration != null && !duration.isNegative() && duration.compareTo(Duration.ofHours(24)) <= 0;
    }

    public String getStatus() {
        return enabled ? "ON" : "OFF";
    }

    public void setTimer(@Valid Duration duration, @Valid Period daysActive) {
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
}
