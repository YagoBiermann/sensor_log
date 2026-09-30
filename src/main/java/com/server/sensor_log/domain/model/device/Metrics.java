package com.server.sensor_log.domain.model.device;

import java.util.Map;

public record Metrics(Map<MetricsType, Double> metrics) {
}
