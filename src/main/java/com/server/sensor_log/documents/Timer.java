package com.server.sensor_log.documents;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Valid
public final class Timer {

    @NotBlank(message = "Timer name must not be blank")
    private String name = "Timer";
    @NotNull
    @Min(value = 0, message = "Hours must be >= 0")
    @Max(value = 23, message = "Hours must be <= 23")
    private Integer hours = 0;
    @NotNull
    @Min(value = 0, message = "Minutes must be >= 0")
    @Max(value = 59, message = "Minutes must be <= 59")
    private Integer minutes = 0;
    @NotNull
    private Boolean isActive = false;

    public String getTimerStatus() {
        if (hours == 0 && minutes == 0) {
            return "OFF";
        } else if (hours == 24 && minutes == 0) {
            return "ON";
        } else {
            return "ON for " + hours + "h " + minutes + "m";
        }
    }

    @Override
    public String toString() {
        return "Timer{"
                + "name='" + name + '\''
                + ", hours=" + hours
                + ", minutes=" + minutes
                + '}';
    }
}
