package com.server.sensor_log.infra.messaging.mqtt.adapter.inbound;

import org.springframework.stereotype.Component;
import com.server.sensor_log.infra.messaging.mqtt.MqttMessageListener;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FanDataListener implements MqttMessageListener {

    @Override
    public String getTopic() {
        return "iot/home/+/fan-+/data";
    }

    @Override
    public void handle(String topic, String payload) {
        log.info("Fan data from {}: {}", topic, payload);
    }
}
