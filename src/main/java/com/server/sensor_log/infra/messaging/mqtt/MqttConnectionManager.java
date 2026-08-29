package com.server.sensor_log.infra.messaging.mqtt;

import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.server.sensor_log.application.workers.ReconnectionWorker;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttConnectionManager {

    private final MqttClientPort mqttClient;
    private final MqttMessageDispatcher dispatcher;
    private final ReconnectionWorker reconnectionWorker;

    @Getter
    private String topic = "iot/#";

    @PostConstruct
    public void start() {
        log.info("🔵 Connecting to MQTT broker...");
        mqttClient.connect()
                .thenCompose(connAck -> {
                    log.info("🟢 Connected successfully to MQTT broker");
                    return mqttClient.subscribe(topic, this::handleMessage);
                })
                .exceptionally(ex -> {
                    log.warn("🟡 MQTT connection failed, scheduling reconnection...");
                    reconnectionWorker.scheduleReconnect(this::tryReconnect);
                    return null;
                });
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

    public void subscribe(String topic) {
        this.topic = topic;
        mqttClient.subscribe(topic, this::handleMessage);
    }

    private void tryReconnect() {
        try {
            mqttClient.reconnect(topic, this::handleMessage).orTimeout(5, TimeUnit.SECONDS).join();
            reconnectionWorker.cancelReconnect();
            log.info("🟢 MQTT reconnected successfully!");

        } catch (CompletionException e) {
            handleReconnectError(e.getCause());
        }
    }

    private void handleReconnectError(Throwable cause) {
        switch (cause) {
            case TimeoutException e ->
                log.warn("🟡 Reconnect timed out. Retrying...");
            default ->
                log.error("🔴 Error while reconnecting", cause);
        }
    }

    public void handleMessage(@NonNull Mqtt5Publish mqttMessage) {
        String pubTopic = mqttMessage.getTopic().toString();
        byte[] payloadBytes = mqttMessage.getPayloadAsBytes();
        String payload = new String(payloadBytes);

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
