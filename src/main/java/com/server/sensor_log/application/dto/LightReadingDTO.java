package com.server.sensor_log.application.dto;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import jakarta.validation.constraints.*;

@Getter @Builder @Jacksonized
public class LightReadingDTO {
    @NotBlank(message = "Id must not be null or blank")
    private String deviceId;

    @NotNull(message = "Intensity is required")
    @Min(value = 0, message = "Intensity must be >= 0")
    @Max(value = 100, message = "Intensity must be <= 100")
    private Integer intensity;

    @NotNull(message = "Voltage is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Voltage must be greater than 0")
    @DecimalMax(value = "240.0", message = "Voltage must not exceed 240V")
    private Double voltage;
}
