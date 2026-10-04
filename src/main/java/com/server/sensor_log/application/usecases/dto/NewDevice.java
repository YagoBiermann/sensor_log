package com.server.sensor_log.application.usecases.dto;

import java.util.List;

import com.server.sensor_log.domain.model.device.Location;
import com.server.sensor_log.domain.model.device.MetricsType;

public record NewDevice(String description, Location location, List<MetricsType> deviceMetrics) {

}
