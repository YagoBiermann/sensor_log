package com.server.sensor_log.mqtt;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class MqttMessageDispatcher {

    private final List<TopicHandler> handlers = new ArrayList<>();

    public MqttMessageDispatcher(List<TopicHandler> topicHandlers) {
        topicHandlers.forEach(this::register);
    }

    public void register(TopicHandler handler) {
        handlers.add(handler);
    }

    public void dispatch(String topic, String payload) {
        handlers.stream()
                .filter(h -> matches(h.getTopic(), topic))
                .forEach(h -> h.handle(topic, payload));
    }

    private boolean matches(String pattern, String topic) {
        String regex = pattern
                .replace("+", "[^/]+")
                .replace("#", ".+");
        return topic.matches(regex);
    }
}
