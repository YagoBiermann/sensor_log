package com.server.sensor_log.dto;

import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.server.sensor_log.documents.Timer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
    @NotBlank(message = "Id must not be null or blank")
    private String id;

    @JsonProperty("name")
    @NotBlank(message = "Name must not be blank")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @JsonProperty("readingTimestamp")
    @NotNull(message = "Reading timestamp is required")
    @Positive(message = "Reading timestamp must be a positive number")
    private Long readingTimestamp;

    @JsonProperty("active")
    @NotNull(message = "Active flag must not be null")
    private Boolean active;

    @JsonProperty("location")
    @Size(max = 255, message = "Location must be at most 255 characters")
    private String location;

    @JsonProperty("intensity")
    @NotNull(message = "Intensity is required")
    @Min(value = 0, message = "Intensity must be >= 0")
    @Max(value = 100, message = "Intensity must be <= 100")
    private Integer intensity;

    @JsonProperty("voltage")
    @NotNull(message = "Voltage is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Voltage must be greater than 0")
    @DecimalMax(value = "240.0", message = "Voltage must not exceed 240V")
    private Double voltage;

    @JsonProperty("timer")
    @Valid
    private Timer timer;
}
