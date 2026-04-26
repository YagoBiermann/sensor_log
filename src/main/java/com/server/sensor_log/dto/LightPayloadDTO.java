package com.server.sensor_log.dto;

import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.server.sensor_log.documents.Timer;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class LightPayloadDTO {

    @Id
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("readingTimestamp")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Long readingTimestamp;
    @JsonProperty("active")
    private Boolean active;
    @JsonProperty("location")
    private String location;
    @JsonProperty("intensity")
    private Integer intensity;
    @JsonProperty("voltage")
    private Double voltage;
    @JsonProperty("timer")
    private Timer timer;
}
