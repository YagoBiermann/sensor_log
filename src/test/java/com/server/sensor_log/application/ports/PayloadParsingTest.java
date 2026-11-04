package com.server.sensor_log.application.ports;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PayloadParsingTest {
    @Test
    void parsesCaseInsensitiveMetricsAndDefaultTimestamp() throws Exception {
        Payload payload = Payload.parse("""
                {"serialNumber":"123456789012","metrics":{"temperature":21.5}}
                """);

        assertEquals("123456789012", payload.getSerialNumber());
        assertEquals(21.5, payload.getMetrics().get(com.server.sensor_log.domain.model.device.MetricsType.TEMPERATURE));
        assertNotNull(payload.getTimestamp());
    }

    @Test
    void rejectsMissingRequiredFields() {
        assertThrows(Exception.class, () -> Payload.parse("""
                {"serialNumber":"","metrics":{}}
                """));
        assertThrows(Exception.class, () -> Payload.parse("not-json"));
    }
}
