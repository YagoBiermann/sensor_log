package com.server.sensor_log.application.controllers.device.dto;

public record FirstBootResponse(String redeemCode, String claimStatus, String connectionStatus, boolean inCooldown, boolean hasExpired) {

}
