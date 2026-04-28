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
        var foundHandlers = handlers.stream()
                .filter(h -> matches(h.getTopic(), topic))
                .toList();

        if (foundHandlers.isEmpty()) {
            log.warn("🟡 No handler found for topic '{}'", topic);
            log.debug("Available handlers: {}",
                    handlers.stream()
                            .map(h -> h.getClass().getSimpleName())
                            .toList());
            return;
        }

        foundHandlers.forEach(h -> {
            try {
                log.debug("Handling topic '{}' with handler {}", topic, h.getClass().getSimpleName());
                h.handle(topic, payload);
            } catch (Exception e) {
                log.error("🔴 Error while handling topic '{}' with handler {}",
                        topic, h.getClass().getSimpleName(), e);
            }
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
