package com.server.sensor_log.services;

import org.springframework.stereotype.Service;

import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.server.sensor_log.mqtt.MqttClientPort;
import com.server.sensor_log.mqtt.MqttMessageDispatcher;
import com.server.sensor_log.workers.ReconnectionWorker;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttService {

    private final MqttClientPort mqttClient;
    private final MqttMessageDispatcher dispatcher;
    private final ReconnectionWorker reconnectionWorker;

    @Getter
    private String topic = "iot/#";

    @PostConstruct
    public void start() {
        log.info("🔵 Connecting to MQTT broker...");
        try {
            mqttClient.connect();
            mqttClient.subscribe(topic, this::handleMessage);
        } catch (Exception e) {
            log.warn("🟡 MQTT connection failed: {}. Service will run without MQTT.", e.getMessage());
            reconnectionWorker.scheduleReconnect(this::tryReconnect);
        }
    }

    public Boolean isConnected() {
        return mqttClient.isConnected();
    }

    public void publish(String topic, String payload) {
        log.info("🔵 Publishing message to topic: {}", topic);
        mqttClient.publish(topic, payload)
                .thenRun(() -> log.info("🟢 Message successfully published to topic: {}", topic))
                .exceptionally(throwable -> {
                    log.error("🔴 Failed to publish message to topic: {}", topic, throwable);
                    return null;
                });
    }

    @PreDestroy
    public void stop() {
        log.info("🔵 Disconnecting from MQTT broker...");
        mqttClient.disconnect();
    }

    public void subscribeToNewTopic(String newTopic) {
        this.topic = newTopic;
        mqttClient.subscribe(newTopic, this::handleMessage);
    }

    private void tryReconnect() {
        try {
            mqttClient.connect();
            mqttClient.subscribe(topic, this::handleMessage);
            reconnectionWorker.cancelReconnect();
            log.info("🟢 MQTT reconnected successfully!");
        } catch (Exception e) {
            log.warn("🟡 Reconnect attempt failed: {}", e.getMessage());
        }
    }

    protected void handleMessage(Mqtt5Publish mqttMessage) {
        String pubTopic = mqttMessage.getTopic().toString();
        byte[] payloadBytes = mqttMessage.getPayloadAsBytes();
        String payload = payloadBytes == null ? "" : new String(payloadBytes);

        if (pubTopic.isBlank() || payload.isBlank()) {
            log.error("🔴 MQTT message rejected: blank fields [topicBlank={}, payloadBlank={}] | topic='{}' payload='{}'",
                    pubTopic.isBlank(), payload.isBlank(), pubTopic, payload);
            return;
        }
        log.info("🔵 Message received on topic: {} | payload: {}", pubTopic, payload);
        try {
            dispatcher.dispatch(pubTopic, payload);
            log.trace("🔵 Message dispatched for topic: {}", pubTopic);
        } catch (Exception e) {
            log.error("🔴 Failed to dispatch message for topic: {} | payload: {}", pubTopic, payload, e);
        }

    }
}
