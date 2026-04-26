package com.server.sensor_log.mqtt;

import org.springframework.stereotype.Component;

@Component
public interface TopicHandler {

    String getTopic();

    void handle(String topic, String payload);
}
