package com.server.sensor_log;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.server.sensor_log.mqtt.configs.MqttConfig;

@SpringBootTest
@ImportAutoConfiguration(exclude = MqttConfig.class)
@ActiveProfiles("test")
class DeviceLogApplicationTests {

    @TestConfiguration
    public class MqttTestConfig {

        @Bean
        @Primary
        public Mqtt5AsyncClient mqtt5AsyncClient() {
            Mqtt5AsyncClient mock = Mockito.mock(Mqtt5AsyncClient.class);

            CompletableFuture<Mqtt5ConnAck> future = CompletableFuture.completedFuture(
                    mock(Mqtt5ConnAck.class)
            );
            when(mock.connect()).thenReturn(future);

            return mock;
        }
    }

    @Test
    void contextLoads() {}
}
