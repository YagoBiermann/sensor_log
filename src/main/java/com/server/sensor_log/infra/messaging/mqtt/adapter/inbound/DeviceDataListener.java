package com.server.sensor_log.infra.messaging.mqtt.adapter.inbound;

import com.server.sensor_log.infra.messaging.mqtt.MqttMessageListener;

public class DeviceDataListener implements MqttMessageListener {
    @Override
    public void handle(String topic, String payload) {
        System.out.println("Received device data from topic: " + topic + ", payload: " + payload);
    }
    
}
