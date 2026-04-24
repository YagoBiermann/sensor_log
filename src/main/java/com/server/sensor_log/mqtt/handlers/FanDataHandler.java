package com.server.sensor_log.mqtt.handlers;

import org.springframework.stereotype.Component;
import com.server.sensor_log.mqtt.TopicHandler;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FanDataHandler implements TopicHandler {

    @Override
    public String getTopic() {
        return "iot/home/+/fan-+/data";
    }

    @Override
    public void handle(String topic, String payload) {
        log.info("Fan data from {}: {}", topic, payload);
    }
}
