package com.server.sensor_log.infra.messaging.mqtt;

import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Component
@Validated
public interface TopicHandler {

    String getTopic();

    void handle(@NotBlank String topic, @NotBlank String payload);
}
