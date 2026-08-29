package com.server.sensor_log.application.dto;

import com.server.sensor_log.domain.model.device.ActionType;
import com.server.sensor_log.domain.model.device.DeviceType;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class TopicDTO {

    private String deviceId;
    private DeviceType deviceType;
    private String location;
    private String subLocation;
    private ActionType action;
}
