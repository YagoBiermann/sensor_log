package com.server.sensor_log.application.integration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.server.sensor_log.application.usecases.ReceiveMetricsDataUseCase;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.MetricsType;
import com.server.sensor_log.infra.messaging.mqtt.MqttConnectionManager;
import com.server.sensor_log.infra.messaging.mqtt.MqttMessageProcessor;
import com.server.sensor_log.infra.repository.DeviceRepository;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

@Testcontainers
class MqttMetricsIntegrationTest {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0");

    @Container
    static final GenericContainer<?> MOSQUITTO = new GenericContainer<>("eclipse-mosquitto:2.0")
            .withExposedPorts(1883)
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("mosquitto-integration.conf"),
                    "/mosquitto/config/mosquitto.conf")
            .withCommand("mosquitto", "-c", "/mosquitto/config/mosquitto.conf");

    private static MongoClient mongoClient;
    private static DeviceRepository deviceRepository;
    private Mqtt5AsyncClient applicationClient;
    private MqttConnectionManager connectionManager;

    @BeforeAll
    static void setUpRepositories() {
        mongoClient = MongoClients.create(MONGO.getReplicaSetUrl());
        MongoTemplate mongoTemplate = new MongoTemplate(
                new SimpleMongoClientDatabaseFactory(mongoClient, "sensor_log_mqtt_integration"));
        MongoRepositoryFactory repositoryFactory = new MongoRepositoryFactory(mongoTemplate);
        deviceRepository = repositoryFactory.getRepository(DeviceRepository.class);
    }

    @AfterAll
    static void closeMongoClient() {
        mongoClient.close();
    }

    @BeforeEach
    void cleanDatabase() {
        deviceRepository.deleteAll();
    }

    @Test
    void persistsMetricsPublishedByDeviceThroughMosquitto() {
        Device device = Device.create(
                "123456789012",
                "",
                "",
                null,
                List.of(MetricsType.TEMPERATURE, MetricsType.HUMIDITY));
        deviceRepository.save(device);

        ReceiveMetricsDataUseCase receiveMetricsDataUseCase =
                new ReceiveMetricsDataUseCase(deviceRepository);
        MqttMessageProcessor messageProcessor =
                new MqttMessageProcessor(receiveMetricsDataUseCase);
        applicationClient = mqttClient("application-client");
        connectionManager = new MqttConnectionManager(applicationClient, messageProcessor);
        connectionManager.start();

        Mqtt5AsyncClient deviceClient = mqttClient("device-client");
        try {
            deviceClient.connect().join();
            await().atMost(Duration.ofSeconds(10))
                    .until(() -> applicationClient.getState().isConnected()
                            && connectionManager.getTopic().equals("iot/+/data"));

            deviceClient.publishWith()
                    .topic("iot/123456789012/data")
                    .payload("""
                            {"serialNumber":"123456789012","metrics":{"TEMPERATURE":22.5,"HUMIDITY":61.0}}
                            """.getBytes())
                    .qos(com.hivemq.client.mqtt.datatypes.MqttQos.AT_LEAST_ONCE)
                    .send()
                    .join();

            await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
                Device persisted = deviceRepository.findById("123456789012").orElseThrow();
                assertEquals(22.5, persisted.getMetrics().get(MetricsType.TEMPERATURE));
                assertEquals(61.0, persisted.getMetrics().get(MetricsType.HUMIDITY));
            });
        } finally {
            deviceClient.disconnect().orTimeout(5, TimeUnit.SECONDS).join();
            connectionManager.stop();
        }
    }

    private Mqtt5AsyncClient mqttClient(String clientId) {
        return MqttClient.builder()
                .useMqttVersion5()
                .identifier(clientId)
                .serverHost(MOSQUITTO.getHost())
                .serverPort(MOSQUITTO.getMappedPort(1883))
                .buildAsync();
    }
}
