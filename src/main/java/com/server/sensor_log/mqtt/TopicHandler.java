package com.server.sensor_log.mqtt;

import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

import com.server.sensor_log.validations.ValidJson;

@Component
@Validated
public interface TopicHandler {

    String getTopic();

    void handle(@NotBlank String topic, @NotBlank @ValidJson String payload);
}
