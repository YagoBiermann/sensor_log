package com.server.sensor_log.mqtt;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

public interface MqttClientPort {
    void connect();
    void disconnect();
    CompletableFuture<Void> publish(String topic, String payload);
    void subscribe(String topic, Consumer<Mqtt5Publish> callback);
}