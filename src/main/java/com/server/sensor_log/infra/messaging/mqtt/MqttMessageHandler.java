package com.server.sensor_log.infra.messaging.mqtt;

import org.springframework.stereotype.Component;

import com.server.sensor_log.application.ports.Payload;
import com.server.sensor_log.application.ports.Topic;
import com.server.sensor_log.application.usecases.ReceiveMetricsDataUseCase;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MqttMessageHandler {

    private final ReceiveMetricsDataUseCase receiveMetricsDataUseCase;

    public void handle(String topic, String payload) {
        try {
            Payload payloadObj = Payload.parse(payload);
            Topic topicObj = Topic.parse(topic);
            receiveMetricsDataUseCase.execute(topicObj, payloadObj);
        } catch (Exception e) {
            System.err.println("Error parsing message: " + e.getMessage());
        }
    }
}