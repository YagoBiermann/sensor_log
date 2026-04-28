package com.server.sensor_log.mqtt;

import java.util.List;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MqttMessageDispatcher {

    private final List<TopicHandler> handlers;

    public MqttMessageDispatcher(List<TopicHandler> handlers) {
        this.handlers = handlers;
        log.info("Registered {} handler(s): {}", handlers.size(),
                handlers.stream().map(h -> h.getClass().getSimpleName()).toList());
    }

    public void register(TopicHandler handler) {
        handlers.add(handler);
    }
    
    public void dispatch(String topic, String payload) {
        handlers.stream()
                .filter(h -> {
                    boolean matches = matches(h.getTopic(), topic);
                    log.debug("Checking if handler {} matches topic '{}'", h.getClass().getSimpleName(), topic);
                    log.debug("Handler topic pattern: '{}'", h.getTopic());
                    log.debug(matches ? "Handler matches topic" : "Handler does not match topic");

                    return matches;
                })
                .forEach(h -> {
                    log.debug("Handling topic '{}' with handler {}", topic, h.getClass().getSimpleName());
                    h.handle(topic, payload);
                });
    }

    private boolean matches(String pattern, String topic) {
        String regex = pattern
                .replace(".", "\\.") // escape literal dots
                .replace("+", "[^/]+") // MQTT single-level wildcard
                .replace("#", ".*");      // MQTT multi-level wildcard
        return topic.matches(regex);
    }
}
