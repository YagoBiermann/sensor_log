package com.server.sensor_log.application.controllers.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DeviceEnrollmentRequestBody(
        @NotBlank
        @Size(max = 8192, message = "csr must contain at most 8192 characters")
        @Pattern(regexp = "(?s)^\\s*-----BEGIN (NEW )?CERTIFICATE REQUEST-----[A-Za-z0-9+/=\\s]+-----END (NEW )?CERTIFICATE REQUEST-----\\s*$", message = "csr must be in PEM format")
        String csr) {
}
