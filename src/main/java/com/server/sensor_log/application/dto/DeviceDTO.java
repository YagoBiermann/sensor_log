package com.server.sensor_log.application.dto;

import com.server.sensor_log.domain.model.device.Timer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@Builder
@Jacksonized
public class DeviceDTO {

    @NotBlank(message = "Id must not be null or blank")
    private String deviceId;
    private List<String> readingIds;
    @NotNull
    private Boolean active;
    @NotBlank(message = "topic must not be null or blank")
    private String topic;
    private Timer timer;

}
