package com.server.sensor_log.application.controllers.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FirstBootRequest(@NotBlank(message = "device serial number is required") @Size(min = 12, max = 12, message = "device serial number must have 12 characters") @Pattern(regexp = "^[A-Za-z0-9]{12}$", message = "device serial number contains invalid characters") String serialNumber, @NotBlank(message = "first boot token is required") String firstBootToken){}