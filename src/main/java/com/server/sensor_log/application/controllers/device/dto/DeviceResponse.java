package com.server.sensor_log.application.controllers.device.dto;

import java.time.Instant;
import java.util.Map;

import com.server.sensor_log.domain.model.device.ClaimStatus;
import com.server.sensor_log.domain.model.device.ConnectionStatus;
import com.server.sensor_log.domain.model.device.Location;
import com.server.sensor_log.domain.model.device.MetricsType;

public record DeviceResponse(
        String serialNumber,
        String ownerId,
        ClaimStatus claimStatus,
        Location location,
        ConnectionStatus connectionStatus,
        Instant createdAt,
        Map<MetricsType, Double> metrics) {
}
