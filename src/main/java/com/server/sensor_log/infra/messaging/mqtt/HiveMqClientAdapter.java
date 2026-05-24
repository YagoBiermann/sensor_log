package com.server.sensor_log.infra.messaging.mqtt;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import static com.hivemq.client.mqtt.MqttClientState.CONNECTING;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HiveMqClientAdapter implements MqttClientPort {

    private final Mqtt5AsyncClient client;
    private final Map<String, Consumer<Mqtt5Publish>> subscriptions = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Mqtt5ConnAck> connect() {
        if (isConnected()) {
            throw new IllegalStateException("Already connected");
        }

        return client.toAsync().connect();
    }

    @Override
    public CompletableFuture<Void> reconnect(String topic, Consumer<Mqtt5Publish> callback) {
        if (client.getState() == CONNECTING || isConnected()) {
            client.disconnect().handle((v, e) -> {
                return null;
            }).join();
        }
        if (subscriptions.isEmpty()) {
            return client.toAsync().connect().thenRun(() -> subscribe(topic, callback));
        }

        return client.toAsync().connect().thenRun(this::resubscribeAll);
    }

    @Override
    public CompletableFuture<Void> disconnect() {
        if (!isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        subscriptions.keySet().forEach(this::tryUnsubscribeFromBroker);
        subscriptions.clear();
        return client.toAsync().disconnect()
                .thenRun(() -> log.info("🟢 MQTT client disconnected successfully!"))
                .exceptionally(throwable -> {
                    log.error("🔴 Failed to disconnect MQTT client", throwable);
                    return null;
                });
    }

    @Override
    public CompletableFuture<Void> publish(String topic, String payload) {
        return client.publishWith()
                .topic(topic)
                .payload(payload.getBytes())
                .qos(MqttQos.AT_LEAST_ONCE)
                .send()
                .thenRun(() -> log.info("🔵 Message published to topic: {}", topic));
    }

    @Override
    public CompletableFuture<Void> subscribe(String topic, Consumer<Mqtt5Publish> callback) {
        if (!isConnected()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("🟡 Cannot subscribe because MQTT client is not connected")
            );
        }

        // Register topic and callback
        boolean alreadySubscribed = subscriptions.containsKey(topic);
        if (alreadySubscribed) {
            log.debug("⚪ Already subscribed to topic: {}, skipping.", topic);
            return CompletableFuture.completedFuture(null);
        }
        return client.toAsync()
                .subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(callback)
                .send()
                .thenRun(() -> {
                    subscriptions.putIfAbsent(topic, callback);
                    log.info("🟢 Successfully subscribed to topic: {}", topic);
                })
                .exceptionally(throwable -> {
                    log.error("🔴 Failed to subscribe to topic: {}", topic, throwable);
                    subscriptions.remove(topic);
                    tryUnsubscribeFromBroker(topic);
                    throw new RuntimeException("MQTT subscribe failed", throwable);
                });
    }

    private Boolean isConnected() {
        return client.getState().isConnected();
    }

    private void resubscribeAll() {
        Set<String> topics = Set.copyOf(subscriptions.keySet());
        if (topics.isEmpty()) {
            log.info("⚪ No topics to resubscribe");
            return;
        }
        log.info("🔵 Resubscribing to {} topics after reconnection", topics.size());

        topics.forEach(topic -> {
            log.debug("🔵 Resubscribing to topic: {}", topic);
            subscribe(topic, subscriptions.get(topic));
        });
    }

    private void tryUnsubscribeFromBroker(String topic) {
        try {
            client.unsubscribeWith()
                    .topicFilter(topic)
                    .send()
                    .get(5, TimeUnit.SECONDS);
            log.info("⚪ Cleaned up broker subscription for topic: {}", topic);
            subscriptions.remove(topic);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.warn("🟡 Could not cleanly unsubscribe from topic: {} during cleanup", topic, e);
        }
    }
}
