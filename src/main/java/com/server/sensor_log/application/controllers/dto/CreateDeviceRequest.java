package com.server.sensor_log.application.controllers.dto;

import java.util.List;

import com.server.sensor_log.domain.model.device.Location;
import com.server.sensor_log.domain.model.device.MetricsType;

public record CreateDeviceRequest(String description, Location location, List<MetricsType> deviceMetrics) {

}
