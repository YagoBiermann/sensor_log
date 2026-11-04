package com.server.sensor_log.application.controllers.device.dto;

import java.util.List;

import com.server.sensor_log.domain.model.device.MetricsType;
import jakarta.validation.constraints.NotEmpty;

public record CreateDeviceRequest(@NotEmpty List<MetricsType> deviceMetrics) {
}
