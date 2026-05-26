package com.server.sensor_log.application.dto;

import com.server.sensor_log.domain.model.device.ActionType;
import com.server.sensor_log.domain.model.device.DeviceType;

public record TopicDTO(
        String deviceId,
        DeviceType deviceType,
        String location,
        String subLocation,
        ActionType action
) {
}