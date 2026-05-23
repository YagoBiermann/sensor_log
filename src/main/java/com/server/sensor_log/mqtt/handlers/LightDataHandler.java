package com.server.sensor_log.mqtt.handlers;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.sensor_log.documents.device.device_readings.LightReading;
import com.server.sensor_log.dto.LightMapper;
import com.server.sensor_log.dto.LightReadingDTO;
import com.server.sensor_log.mqtt.TopicHandler;
import com.server.sensor_log.repository.SensorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class LightDataHandler implements TopicHandler {

    private final SensorRepository repository;
    private final LightMapper lightMapper;

    private final ObjectMapper objectMapper;

    @Override
    public String getTopic() {
        return "iot/home/+/light-+/data";
    }

    @Override
    public void handle(String topic, String payload) {
        log.info("🔵 Light data received from topic '{}': {}", topic, payload);
        try {
            log.debug("🟡 Attempting to deserialize payload {} from topic '{}'", payload, topic);
            LightReadingDTO dto = objectMapper.readValue(payload, LightReadingDTO.class);
            log.debug("🔵 Payload deserialized successfully: {}", dto);
            LightReading lightReading = lightMapper.toEntity(dto);
            log.debug("🔵 Mapped LightPayloadDTO to Light entity: {}", lightReading);

            repository.save(lightReading);
            log.info("🟢 Light entity saved successfully from topic '{}'", topic);
        } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
            log.error("🔴 Deserialization error (invalid structure) on topic '{}': {}", topic, e.getMessage());

        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            log.error("🔴 Mapping error during deserialization on topic '{}': {}", topic, e.getMessage());

        } catch (JsonProcessingException e) {
            log.error("🔴 Generic JSON processing error on topic '{}': {}", topic, e.getMessage());

        } catch (Exception e) {
            log.error("🔴 Unexpected error processing light data from topic '{}': {}", topic, e.getMessage());
        }
    }
}
