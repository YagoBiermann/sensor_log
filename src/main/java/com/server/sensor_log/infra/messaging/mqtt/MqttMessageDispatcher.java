package com.server.sensor_log.infra.messaging.mqtt;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MqttMessageDispatcher {

    private final List<MqttMessageListener> handlers;

    public MqttMessageDispatcher(List<MqttMessageListener> handlers) {
        this.handlers = handlers;
        log.info("Registered {} handler(s): {}", handlers.size(),
                handlers.stream().map(h -> h.getClass().getSimpleName()).toList());
    }

    public void register(MqttMessageListener handler) {
        handlers.add(handler);
    }

    public void dispatch(String topic, String payload) {
        var foundHandlers = handlers.stream()
                .filter(h -> matches(topic))
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

    private boolean matches(String topic) {
        String[] parts = topic.split("/");

        return parts.length == 3
                && parts[0].equals("iot")
                && parts[1].matches("\\d{12}") // serial number(12 digits)
                && parts[2].equals("data");
    }
}
