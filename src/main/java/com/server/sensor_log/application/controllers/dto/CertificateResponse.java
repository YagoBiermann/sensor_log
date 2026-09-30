package com.server.sensor_log.application.controllers.dto;

public record CertificateResponse(
    String certificate,
    String serialNumber,
    String expiresAt) {
}