package com.server.sensor_log.documents;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TypeAlias("light")
@Document(collection = "lights")
@NoArgsConstructor
@AllArgsConstructor
public class Light extends Sensor {

    private DeviceController intensity;
    private Timer timer;
    private Double voltage = 0.0;

    public void setTimer(Integer hours, Integer minutes) {
        if (this.timer == null) {
            throw new IllegalStateException("Timer not set");
        }
        this.timer.setTimer(hours, minutes);
    }

    @Override
    public String toString() {
        return "Light(" + this.getId() + "){"
                + "status=" + (this.getActive() ? "ON" : "OFF")
                + ", intensity=" + this.getIntensity() + "%"
                + ", voltage=" + this.getVoltage() + "w"
                + ", Timer Status=" + (this.timer != null ? this.timer.getTimerStatus() : "N/A")
                + '}';
    }
}
