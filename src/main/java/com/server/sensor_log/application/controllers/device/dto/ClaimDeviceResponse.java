package com.server.sensor_log.application.controllers.device.dto;

import com.server.sensor_log.domain.model.device.ClaimStatus;

public record ClaimDeviceResponse(String serialNumber, String ownerId, ClaimStatus claimStatus) {

}
