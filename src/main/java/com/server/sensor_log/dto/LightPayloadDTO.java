package com.server.sensor_log.dto;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;
import com.server.sensor_log.documents.Timer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Getter
@Builder
@Jacksonized
public class LightPayloadDTO {

    @Id
    @NotBlank(message = "Id must not be null or blank")
    private String id;

    @NotBlank(message = "Name must not be blank")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @NotNull(message = "Reading timestamp is required")
    @Positive(message = "Reading timestamp must be a positive number")
    private Long readingTimestamp;

    @NotNull(message = "Active flag must not be null")
    private Boolean active;

    @Size(max = 255, message = "Location must be at most 255 characters")
    private String location;

    @NotNull(message = "Intensity is required")
    @Min(value = 0, message = "Intensity must be >= 0")
    @Max(value = 100, message = "Intensity must be <= 100")
    private Integer intensity;

    @NotNull(message = "Voltage is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Voltage must be greater than 0")
    @DecimalMax(value = "240.0", message = "Voltage must not exceed 240V")
    private Double voltage;

    private Timer timer;
}
