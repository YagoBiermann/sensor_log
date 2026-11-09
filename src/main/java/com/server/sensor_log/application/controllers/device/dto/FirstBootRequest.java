package com.server.sensor_log.application.controllers.device.dto;

public record FirstBootRequest(@NotBlank(message = "device serial number is required") @Size(min = 12, max = 12, message = "device serial number must have 12 characters") @Pattern(regexp = "^[A-Za-z0-9]{12}$", message = "device serial number contains invalid characters") String serialNumber, @NotBlank(message = "first boot token is required") String firstBootToken, @NotBlank(message = "csr is required") @Size(max = 8192, message = "csr must be in English") @Pattern(regexp = "(?s)^\\s*-----BEGIN (NEW )?CERTIFICATE REQUEST-----[A-Za-z0-9+/=\\s]+-----END (NEW )?CERTIFICATE REQUEST-----\\s*$", message = "csr must be in PEM format") String csr) {

}
