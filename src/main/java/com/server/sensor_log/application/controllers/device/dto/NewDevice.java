package com.server.sensor_log.application.controllers.device.dto;

import java.util.List;

import com.server.sensor_log.domain.model.device.MetricsType;

public record NewDevice(List<MetricsType> deviceMetrics) {

}
