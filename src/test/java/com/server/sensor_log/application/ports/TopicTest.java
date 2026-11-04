package com.server.sensor_log.application.ports;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TopicTest {
    @Test
    void buildsAndParsesDataTopic() {
        Topic topic = new Topic("123456789012", "data");

        assertEquals("iot/123456789012/data", topic.getTopic());
        Topic parsed = Topic.parse(topic.getTopic());
        assertEquals("123456789012", parsed.getDeviceSerialNumber());
        assertEquals("data", parsed.getActionType());
    }

    @Test
    void parsesCommandTopic() {
        Topic parsed = Topic.parse("iot/ABC123456789/command");

        assertEquals("ABC123456789", parsed.getDeviceSerialNumber());
        assertEquals("command", parsed.getActionType());
    }

    @Test
    void rejectsMalformedTopics() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> Topic.parse("iot/123456789012")),
                () -> assertThrows(IllegalArgumentException.class, () -> Topic.parse("mqtt/123456789012/data")),
                () -> assertThrows(IllegalArgumentException.class, () -> Topic.parse("iot/12345678901/data")),
                () -> assertThrows(IllegalArgumentException.class, () -> Topic.parse("iot/123456789012/other")),
                () -> assertThrows(IllegalArgumentException.class, () -> Topic.parse("iot/1234-56789012/data")));
    }
}
