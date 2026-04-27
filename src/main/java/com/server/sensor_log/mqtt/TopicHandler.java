package com.server.sensor_log.mqtt;

import jakarta.validation.constraints.NotBlank;

public interface TopicHandler {

    String getTopic();

    void handle(@NotBlank String topic, @NotBlank String payload);
}
