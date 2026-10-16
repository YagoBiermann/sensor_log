package com.server.sensor_log.infra.messaging.mqtt.configs;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {

    @NotBlank(message = "mqtt.host must not be blank")
    public String host;

    @Min(value = 1, message = "mqtt.port must be greater than 0")
    @Max(value = 65535, message = "mqtt.port must be lower than 65536")
    public int port;

    @NotBlank(message = "mqtt.client-id must not be blank")
    public String clientId;
    public String username;
    public String password;
    public boolean sslEnabled;
    public String keystorePath;
    public String keystorePassword;
}