package com.server.sensor_log.documents.device.device_readings;

import com.server.sensor_log.documents.device.Device;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@TypeAlias("temperature")
@Document(collection = "temperatures")
@Slf4j
public class TempReading extends Device {
    @Builder.Default
    @NonNull
    private Integer temperature = 0;     // °C
    @Builder.Default
    @NonNull
    private Integer humidity = 0;        // %
    @Builder.Default
    @NonNull
    private Double ph = 0.0;             // pH
    @Builder.Default
    public String type = "TEMPERATURE";
    @Builder.Default
    public Boolean active = isActive();

    public Boolean isActive(){
        return temperature != 0 && humidity != 0 && ph != 0;
    }

    @Override
    public String toString() {
        return "Temperature(" + this.getId() + "){"
                + "status=" + (this.getActive() ? "ON" : "OFF")
                + ", temperature=" + this.getTemperature() + "°C"
                + ", humidity=" + this.getHumidity() + "%"
                + ", ph=" + this.getPh() + "pH"
                + '}';
    }
}
