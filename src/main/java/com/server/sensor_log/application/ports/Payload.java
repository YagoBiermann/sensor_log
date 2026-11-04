package com.server.sensor_log.application.ports;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.server.sensor_log.domain.model.device.MetricsType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Payload {
        @NotBlank(message = "Serial number must be provided")
        private String serialNumber;
        @NotEmpty(message = "Metrics must be provided")
        private Map<MetricsType, Double> metrics;
        private Instant timestamp;

        public Payload() {
        }

        public Payload(String serialNumber, Map<MetricsType, Double> metrics) {
                this.serialNumber = serialNumber;
                this.timestamp = Instant.now();
                this.metrics = metrics;
        }

        public String getSerialNumber() {
                return serialNumber;
        }

        public Instant getTimestamp() {
                return timestamp;
        }

        public Map<MetricsType, Double> getMetrics() {
                return metrics;
        }

        public static Payload parse(String data) throws JsonMappingException, JsonProcessingException {
                JsonMapper jsonMapper = JsonMapper.builder()
                                .addModule(new JavaTimeModule())
                                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
                                .build();
                Payload payload = jsonMapper.readValue(data, Payload.class);
                if (payload.serialNumber == null || payload.serialNumber.isBlank()
                                || payload.metrics == null || payload.metrics.isEmpty()) {
                        throw new IllegalArgumentException("Payload must contain serialNumber and metrics");
                }
                if (payload.timestamp == null) {
                        payload.timestamp = Instant.now();
                }

                return payload;
        }
}
