package com.server.sensor_log.application.dto;

import com.server.sensor_log.domain.model.device.Timer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder @Jacksonized
public record DeviceDTO(
        @NotBlank(message = "Id must not be null or blank")
        String deviceId,
        List<String> readingIds,
        @NotNull
        Boolean active,
        @NotBlank(message = "topic must not be null or blank")
        String topic,
        Timer timer
) {
}
