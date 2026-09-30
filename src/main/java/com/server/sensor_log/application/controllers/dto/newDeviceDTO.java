package com.server.sensor_log.application.controllers.dto;

import java.util.List;

import com.server.sensor_log.domain.model.device.MetricsType;

public record newDeviceDTO(String description, List<MetricsType> deviceMetrics) {

}
