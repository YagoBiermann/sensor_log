package com.server.sensor_log.mqtt;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HiveMqClientAdapter implements MqttClientPort {

    private final Mqtt5AsyncClient client;

    @Override
    public void connect() {
        try {
            client.connect().get(10, TimeUnit.SECONDS);
            log.info("🟢 Successfully connected to MQTT broker");
        } catch (TimeoutException e) {
            log.error("🔴 Connection timed out", e);
            throw new RuntimeException("MQTT connection timed out", e);
        } catch (ExecutionException e) {
            log.error("🔴 Failed to connect to MQTT broker", e.getCause());
            throw new RuntimeException("MQTT connection failed", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("🔴 Connection interrupted", e);
            throw new RuntimeException("MQTT connection interrupted", e);
        }
    }

    @Override
    public void disconnect() {
        try {
            client.disconnect().get(10, TimeUnit.SECONDS);
            log.info("🔌 Disconnected from MQTT broker");
        } catch (TimeoutException e) {
            log.error("🔴 Disconnection timed out", e);
            throw new RuntimeException("MQTT disconnection timed out", e);
        } catch (ExecutionException e) {
            log.error("🔴 Failed to disconnect from MQTT broker", e.getCause());
            throw new RuntimeException("MQTT disconnection failed", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("🔴 Disconnection interrupted", e);
            throw new RuntimeException("MQTT disconnection interrupted", e);
        }
    }

    @Override
    public CompletableFuture<Void> publish(String topic, String payload) {
        return client.publishWith()
                .topic(topic)
                .payload(payload.getBytes())
                .qos(MqttQos.AT_LEAST_ONCE)
                .send()
                .thenAccept(result -> log.info("📤 Message published to topic: {}", topic))
                .exceptionally(throwable -> {
                    log.error("🔴 Failed to publish to topic: {}", topic, throwable);
                    throw new RuntimeException("MQTT publish failed", throwable);
                });
    }

    @Override
    public void subscribe(String topic, Consumer<Mqtt5Publish> callback) {
        try {
            client.subscribeWith()
                    .topicFilter(topic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .callback(callback)
                    .send()
                    .get(10, TimeUnit.SECONDS);
            log.info("📥 Subscribed to topic: {}", topic);
        } catch (TimeoutException e) {
            log.error("🔴 Subscribe timed out on topic: {}", topic, e);
            throw new RuntimeException("MQTT subscribe timed out", e);
        } catch (ExecutionException e) {
            log.error("🔴 Failed to subscribe to topic: {}", topic, e.getCause());
            throw new RuntimeException("MQTT subscribe failed", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("🔴 Subscribe interrupted on topic: {}", topic, e);
            throw new RuntimeException("MQTT subscribe interrupted", e);
        }
    }
}
