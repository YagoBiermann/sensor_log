package com.server.sensor_log.application.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

public record RegisterRequest(@NotBlank @Email String email, @NotBlank @Size(min = 8, max = 64) String password, String name) {
}
