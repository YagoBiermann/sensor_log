package com.server.sensor_log.documents;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import lombok.Setter;
import lombok.Getter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Sensor {

    @Id
    private String id = null;
    private String name = "Generic Device";
    private Long readingTimestamp = System.currentTimeMillis();
    private Boolean active = false;
    private String location = "Unknown";
}
