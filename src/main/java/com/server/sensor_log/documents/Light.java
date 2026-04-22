package com.server.sensor_log.documents;

import org.springframework.data.annotation.TypeAlias;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TypeAlias("light")
public class Light extends Sensor {

    @Override
    public String toString() {
        return "Light(" + deviceId + "){"
                + "status=" + status
                + ", intensity=" + intensity + "%"
                + ", voltage=" + voltage + "w"
                + ", TimerOn=" + timer + "h"
                + ", timerOff=" + (timer - 24) + "h"
                + '}';
    }
}
