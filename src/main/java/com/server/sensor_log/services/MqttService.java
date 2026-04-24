package com.server.sensor_log.services;

import org.springframework.stereotype.Service;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.server.sensor_log.mqtt.MqttMessageDispatcher;
import com.server.sensor_log.mqtt.handlers.FanDataHandler;
import com.server.sensor_log.mqtt.handlers.LightDataHandler;
import com.server.sensor_log.mqtt.handlers.SensorDataHandler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttService {

    private final Mqtt5AsyncClient mqttClient;
    private final MqttMessageDispatcher dispatcher;
    
    @PostConstruct
    public void connect() {
        log.info("Connecting to MQTT broker...");
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
    @PostConstruct
    public void registerHandlers() {
        log.info("Registering MQTT topic handlers...");
        dispatcher.register(new SensorDataHandler());
        dispatcher.register(new FanDataHandler());
        dispatcher.register(new LightDataHandler());
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
                .topicFilter("iot/#")
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(this::handleMessage)
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        log.error("Error during subscription setup", throwable);
                    } else {
                        log.info("Subscribed successfully to topic: iot/#");
                    }
                });
    }

    private void handleMessage(Mqtt5Publish mqttMessage) {
        String topic = mqttMessage.getTopic().toString();
        String payload = new String(mqttMessage.getPayloadAsBytes());
        dispatcher.dispatch(topic, payload);
    }

    @PreDestroy
    public void disconnect() {
        mqttClient.disconnect()
                .whenComplete((v, t) -> log.info("Disconnected from MQTT broker"));
    }
}
