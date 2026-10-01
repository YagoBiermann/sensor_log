package com.server.sensor_log.application.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

public record registerRequest(@NotBlank @Email String email, @NotBlank @Size(min = 8) String password) {
}
