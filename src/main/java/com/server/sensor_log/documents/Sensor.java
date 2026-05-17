package com.server.sensor_log.documents;

import lombok.*;

import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.validation.annotation.Validated;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public abstract class Sensor {

    @Id
    private String id = null;
    private String name = "Generic Device";
    private Long readingTimestamp = System.currentTimeMillis();
    private Boolean active = false;
    private String location = "Unknown";
}
