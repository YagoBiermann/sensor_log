package com.server.sensor_log.infra.messaging.mqtt;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

public interface MqttClientPort {
    CompletableFuture<Mqtt5ConnAck> connect();
    CompletableFuture<Void> disconnect();
    CompletableFuture<Void> reconnect(String topic, Consumer<Mqtt5Publish> callback);
    CompletableFuture<Void> publish(String topic, String payload);
    CompletableFuture<Void> subscribe(String topic, Consumer<Mqtt5Publish> callback);
}