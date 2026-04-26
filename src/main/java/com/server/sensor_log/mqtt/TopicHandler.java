package com.server.sensor_log.mqtt;

public interface TopicHandler {

    String getTopic();

    void handle(String topic, String payload);
}
