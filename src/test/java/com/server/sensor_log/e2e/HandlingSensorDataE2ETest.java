package com.server.sensor_log.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.sensor_log.AbstractIntegrationTest;
import com.server.sensor_log.documents.Light;
import com.server.sensor_log.dto.LightPayloadDTO;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class HandlingSensorDataE2ETest extends AbstractIntegrationTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should receive MQTT message and persist Light entity")
    void shouldPersistLightEntityFromMqttMessage() throws Exception {

        LightPayloadDTO dto = LightPayloadDTO.builder()
                .id("light-1")
                .name("Grow Light")
                .readingTimestamp(System.currentTimeMillis())
                .active(true)
                .location("Greenhouse")
                .intensity(85)
                .voltage(220.0)
                .build();

        String payload = objectMapper.writeValueAsString(dto);

        publisher.publishWith()
                .topic("iot/home/living-room/light-1/data")
                .payload(payload.getBytes())
                .send();

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {

                    var sensor = repository.findById("light-1");

                    assertThat(sensor).isPresent();

                    Light light = (Light) sensor.get();

                    assertThat(light.getName()).isEqualTo("Grow Light");
                    assertThat(light.getIntensity()).isEqualTo(85);
                    assertThat(light.getVoltage()).isEqualTo(220.0);
                    assertThat(light.getActive()).isTrue();
                });
    }
}