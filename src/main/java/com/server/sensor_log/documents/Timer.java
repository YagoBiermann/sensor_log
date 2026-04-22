package com.server.sensor_log.documents;

import lombok.Data;

@Data
public class Timer {

    private String name = "generic timer";
    private Integer hours = 0;
    private Integer minutes = 0;


    public Timer() {
    }

    public Timer(String name, Integer hours, Integer minutes) {
        this.name = name;
        this.hours = hours;
        this.minutes = minutes;
    }

    public void setTimer(Integer hours, Integer minutes) {
        if(hours == 24 && minutes == 0) {
            this.hours = hours;
            this.minutes = minutes;
            return;
        }

        if(hours == null || minutes == null) {
            throw new IllegalArgumentException("Hours and minutes cannot be null");
        }
        if (hours < 0 || hours > 24) {
            throw new IllegalArgumentException("Hours value cannot be negative or exceed 24");
        }
        if (minutes < 0 || minutes > 59) {
            throw new IllegalArgumentException("Minutes value cannot be negative or exceed 59");
        }
        this.hours = hours;
        this.minutes = minutes;
    }
}
