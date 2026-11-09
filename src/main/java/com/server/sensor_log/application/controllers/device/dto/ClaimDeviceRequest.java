package com.server.sensor_log.application.controllers.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClaimDeviceRequest(
        @NotBlank(message = "Device serial number is required") @Size(min = 12, max = 12, message = "Serial number must contain 12 characters") @Pattern(regexp = "^[A-Za-z0-9]{12}$", message = "Serial number contains invalid characters") String serialNumber,
        @NotBlank(message = "User ID is required") @Size(max = 254, message = "Email must contain at most 254 characters") String ownerId,
        @NotBlank(message = "Product code is required") @Size(min = 4, max = 4, message = "Product code must contain 4 characters") @Pattern(regexp = "^[A-Za-z0-9_-]*$", message = "Product code contains invalid characters") String redeemCode,
        @NotBlank(message = "csr is required") @Size(max = 8192, message = "csr must contain at most 8192 characters") @Pattern(regexp = "(?s)^\\s*-----BEGIN (NEW )?CERTIFICATE REQUEST-----[A-Za-z0-9+/=\\s]+-----END (NEW )?CERTIFICATE REQUEST-----\\s*$", message = "csr must be in PEM format") String csr) {
}