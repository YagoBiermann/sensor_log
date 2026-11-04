package com.server.sensor_log.application.controllers.device.dto;

import jakarta.validation.constraints.NotBlank;

public record ClaimDeviceRequestBody(
        @NotBlank String redeemCode,
        @NotBlank String csr
) {}