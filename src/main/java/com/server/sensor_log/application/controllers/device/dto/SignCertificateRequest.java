package com.server.sensor_log.application.controllers.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignCertificateRequest(
        @NotBlank(message = "número serial do dispositivo é obrigatório") @Size(min = 12, max = 12, message = "número serial deve ter 12 caracteres") @Pattern(regexp = "^[A-Za-z0-9]{12}$", message = "número serial contém caracteres inválidos") String serialNumber,
        @NotBlank(message = "Id do usuário não fornecido") @Size(max = 254, message = "email deve ter no máximo 254 caracteres") String ownerId,
        @NotBlank(message = "código do produto é obrigatório") @NotBlank(message = "código do produto é obrigatório") @Size(min = 4, max = 4, message = "código do produto inválido") @Pattern(regexp = "^[A-Za-z0-9_-]*$", message = "código do produto contém caracteres inválidos") String redeemToken,
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "token de bootstrap possui caracteres inválidos") @Size(min = 43, max = 43, message = "token de bootstrap deve ter 12 caracteres") String bootstrapToken,
        @NotBlank(message = "csr é obrigatório") @Size(max = 8192, message = "csr excede o tamanho máximo") @Pattern(regexp = "(?s)^\\s*-----BEGIN (NEW )?CERTIFICATE REQUEST-----[A-Za-z0-9+/=\\s]+-----END (NEW )?CERTIFICATE REQUEST-----\\s*$", message = "csr deve estar em formato PEM") String csr,
        @Pattern(regexp = "^[1-9]\\d{0,6}[smhd]?$", message = "ttl inválido (ex: 720h, 30m, 3600)") String ttl,
        @NotBlank(message = "common_name é obrigatório") @Size(max = 64, message = "common_name deve ter no máximo 64 caracteres") String commonName) {
}