package com.server.sensor_log.infra.messaging.mqtt;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MqttConnectionManager {

    private final Mqtt5AsyncClient mqttClient;
    private final MqttMessageProcessor messageProcessor;
    private final Map<String, Consumer<Mqtt5Publish>> subscriptions = new ConcurrentHashMap<>();

    private String topic = "iot/+/data";

    public MqttConnectionManager(
            Mqtt5AsyncClient mqttClient,
            MqttMessageProcessor messageProcessor) {
        this.mqttClient = mqttClient;
        this.messageProcessor = messageProcessor;
    }

    @PostConstruct
    public void start() {
        log.info("Connecting to MQTT broker...");
        mqttClient.connect()
                .thenCompose(connAck -> {
                    log.info("Connected successfully to MQTT broker");
                    return subscribe(topic, this::onMessages);
                })
                .exceptionally(ex -> {
                    log.error("Initial MQTT connection failed. Automatic reconnection will handle connection loss.",
                            ex);
                    return null;
                });
    }

    public CompletableFuture<Void> publish(String topic, String payload) {
        return mqttClient.publishWith()
                .topic(topic)
                .payload(payload.getBytes(StandardCharsets.UTF_8))
                .qos(MqttQos.AT_LEAST_ONCE)
                .send()
                .thenRun(() -> log.info("Message published to topic: {}", topic))
                .exceptionally(throwable -> {
                    log.error("Failed to publish message to topic: {}", topic, throwable);
                    throw new CompletionException("MQTT publish failed", throwable);
                });
    }

    public void subscribe(String topic) {
        this.topic = topic;
        subscribe(topic, this::onMessages);
    }

    public String getTopic() {
        return topic;
    }

    @PreDestroy
    public void stop() {
        log.info("Disconnecting from MQTT broker...");
        disconnect();
    }

    public void onMessages(@NonNull Mqtt5Publish mqttMessage) {
        messageProcessor.process(mqttMessage);
    }

    private CompletableFuture<Void> subscribe(String topic, Consumer<Mqtt5Publish> callback) {
        if (!mqttClient.getState().isConnected()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Cannot subscribe because MQTT client is not connected"));
        }
        if (subscriptions.containsKey(topic)) {
            return CompletableFuture.completedFuture(null);
        }

        return mqttClient.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(callback)
                .send()
                .thenRun(() -> subscriptions.putIfAbsent(topic, callback))
                .exceptionally(throwable -> {
                    subscriptions.remove(topic);
                    tryUnsubscribeFromBroker(topic);
                    throw new CompletionException("MQTT subscribe failed", throwable);
                });
    }

    private CompletableFuture<Void> disconnect() {
        if (!mqttClient.getState().isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        subscriptions.keySet().forEach(this::tryUnsubscribeFromBroker);
        subscriptions.clear();
        return mqttClient.disconnect();
    }

    private void tryUnsubscribeFromBroker(String topic) {
        try {
            mqttClient.unsubscribeWith().topicFilter(topic).send().get(5, TimeUnit.SECONDS);
            subscriptions.remove(topic);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while unsubscribing from topic: {}", topic, exception);
        } catch (ExecutionException | TimeoutException exception) {
            log.warn("Could not unsubscribe from topic: {}", topic, exception);
        }
    }

}
