package com.server.sensor_log.application.ports;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.server.sensor_log.domain.model.device.MetricsType;

class PayloadTest {
    @Test
    void constructorSetsValuesAndTimestamp() {
        Map<MetricsType, Double> metrics = Map.of(MetricsType.TEMPERATURE, 21.5);
        Instant before = Instant.now();
        Payload payload = new Payload("123456789012", metrics);
        Instant after = Instant.now();

        assertAll(
                () -> assertEquals("123456789012", payload.getSerialNumber()),
                () -> assertEquals(metrics, payload.getMetrics()),
                () -> assertFalse(payload.getTimestamp().isBefore(before)),
                () -> assertFalse(payload.getTimestamp().isAfter(after)));
    }

    @Test
    void parsesPayloadWithTimestampAndCaseInsensitiveMetricNames() throws Exception {
        Payload payload = Payload.parse("""
                {"serialNumber":"123456789012","metrics":{"temperature":22.75},"timestamp":"2025-02-03T04:05:06Z"}
                """);

        assertEquals("123456789012", payload.getSerialNumber());
        assertEquals(Map.of(MetricsType.TEMPERATURE, 22.75), payload.getMetrics());
        assertEquals(Instant.parse("2025-02-03T04:05:06Z"), payload.getTimestamp());
    }

    @Test
    void parsesPayloadWithoutTimestampByGeneratingOne() throws Exception {
        Instant before = Instant.now();
        Payload payload = Payload.parse(
                "{\"serialNumber\":\"123456789012\",\"metrics\":{\"HUMIDITY\":60.0}}");

        assertEquals("123456789012", payload.getSerialNumber());
        assertEquals(60.0, payload.getMetrics().get(MetricsType.HUMIDITY));
        assertFalse(payload.getTimestamp().isBefore(before));
    }

    @Test
    void rejectsMissingOrEmptyRequiredFields() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Payload.parse("{\"metrics\":{\"TEMPERATURE\":1.0}}")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Payload.parse("{\"serialNumber\":\"   \",\"metrics\":{\"TEMPERATURE\":1.0}}")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Payload.parse("{\"serialNumber\":\"123456789012\",\"metrics\":{}}")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Payload.parse("{\"serialNumber\":\"123456789012\",\"metrics\":null}")));
    }

    @Test
    void rejectsMalformedJsonAndUnknownMetricNames() {
        assertAll(
                () -> assertThrows(Exception.class, () -> Payload.parse("not-json")),
                () -> assertThrows(Exception.class,
                        () -> Payload.parse("{\"serialNumber\":\"123456789012\",\"metrics\":{\"UNKNOWN\":1}}")));
    }
}
