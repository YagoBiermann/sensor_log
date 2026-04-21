package com.server.sensor_log.services;

import org.springframework.stereotype.Service;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttService {

    private final Mqtt5AsyncClient mqttClient;

    @PostConstruct
    public void connect() {
        mqttClient.connect()
                .whenComplete((ack, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to connect to MQTT broker", throwable);
                    } else {
                        log.info("Connected to MQTT broker: {}", ack);
                        subscribeToTopics();
                    }
                });
    }

    public void publish(String topic, String payload) {
        mqttClient.publishWith()
                .topic(topic)
                .payload(payload.getBytes())
                .qos(MqttQos.AT_LEAST_ONCE)
                .retain(false)
                .send()
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to publish to topic {}", topic, throwable);
                    } else {
                        log.info("Published to {}: {}", topic, payload);
                    }
                });
    }

    private void subscribeToTopics() {
        mqttClient.subscribeWith()
                .topicFilter("sensors/#")
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(publish -> {
                    String topic = publish.getTopic().toString();
                    String payload = new String(publish.getPayloadAsBytes());
                    log.info("Received on {}: {}", topic, payload);
                    handleMessage(topic, payload);
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        log.error("Subscription failed", throwable);
                    } else {
                        log.info("Subscribed successfully");
                    }
                });
    }

    private void handleMessage(String topic, String payload) {
        log.info("Handling message on {}: {}", topic, payload);
    }

    @PreDestroy
    public void disconnect() {
        mqttClient.disconnect()
                .whenComplete((v, t) -> log.info("Disconnected from MQTT broker"));
    }
}
