package com.server.sensor_log;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.server.sensor_log.application.ports.DeviceRepositoryPort;
import com.server.sensor_log.application.ports.LightRepositoryPort;
import com.server.sensor_log.infra.repository.DeviceRepository;
import com.server.sensor_log.infra.repository.LightDataReadingRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
public abstract class AbstractIntegrationTest {
    private static final Network network = Network.newNetwork();
    @Container
    static MongoDBContainer mongo =
            new MongoDBContainer("mongo:7.0")
                    .withReplicaSet()
                    .withNetwork(network)
                    .withNetworkAliases("mongo")
                    .waitingFor(
                            Wait.forListeningPort()
                    );

    @Container
    static GenericContainer<?> mosquitto =
            new GenericContainer<>("eclipse-mosquitto:2.0")
                    .withExposedPorts(1883)
                    .withNetwork(network)
                    .withNetworkAliases("mqtt")
                    .withClasspathResourceMapping(
                            "mosquitto.conf",
                            "/mosquitto/config/mosquitto.conf",
                            BindMode.READ_ONLY
                    )
                    .waitingFor(Wait.forListeningPort());

    protected static Mqtt5BlockingClient publisher;
    @Autowired
    protected DeviceRepository deviceRepository;
    @Autowired
    protected LightDataReadingRepository lighReadingRepository;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", () -> mongo.getReplicaSetUrl());

        registry.add("mqtt.host", mosquitto::getHost);
        registry.add("mqtt.port", () -> mosquitto.getMappedPort(1883));
        registry.add("mqtt.clientId", () -> "sensor-log-test-client");
    }

    @BeforeEach
    void cleanupDatabase() {
        deviceRepository.deleteAll();
        lighReadingRepository.deleteAll();
    }

    @BeforeAll
    static void setupPublisher() {
        publisher = MqttClient.builder()
                .useMqttVersion5()
                .identifier("test-publisher")
                .serverHost(mosquitto.getHost())
                .serverPort(mosquitto.getMappedPort(1883))
                .buildBlocking();

        publisher.connect();
    }

    @AfterAll
    static void cleanupPublisher() {
        if (publisher != null) {
            publisher.disconnect();
        }
    }
}
