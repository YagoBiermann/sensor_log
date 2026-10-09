package com.server.sensor_log.application.controllers.device.dto;

public record ClaimDeviceRequestBody(
        String redeemCode,
        String csr
) {}