package com.server.sensor_log.infra.messaging.mqtt;

import java.nio.charset.StandardCharsets;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.server.sensor_log.application.ports.Payload;
import com.server.sensor_log.application.ports.Topic;
import com.server.sensor_log.application.usecases.ReceiveMetricsDataUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MqttMessageProcessor {

    private final ReceiveMetricsDataUseCase receiveMetricsDataUseCase;

    public void process(@NonNull Mqtt5Publish mqttMessage) {
        String topic = mqttMessage.getTopic().toString();
        String payload = new String(mqttMessage.getPayloadAsBytes(), StandardCharsets.UTF_8);

        if (topic.isBlank() || payload.isBlank()) {
            log.error("MQTT message rejected because topic or payload is blank");
            return;
        }

        try {
            receiveMetricsDataUseCase.execute(Topic.parse(topic), Payload.parse(payload));
        } catch (Exception exception) {
            log.error("Failed to handle MQTT message for topic: {}", topic, exception);
        }
    }
}
