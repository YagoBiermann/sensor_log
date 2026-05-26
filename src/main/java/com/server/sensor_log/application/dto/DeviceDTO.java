package com.server.sensor_log.application.dto;

import com.server.sensor_log.domain.model.device.DeviceType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Jacksonized
public class DeviceDTO {
    @Id
    @NotBlank(message = "Id must not be null or blank")
    private String id;
    private List<String> readingId;
    private Boolean active;
    private String location;
    private DeviceType type;
    private String topic;
}
