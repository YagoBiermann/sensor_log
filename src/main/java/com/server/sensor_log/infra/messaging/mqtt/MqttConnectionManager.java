package com.server.sensor_log.infra.messaging.mqtt;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttClientAutoReconnect;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.server.sensor_log.application.ports.Payload;
import com.server.sensor_log.application.ports.Topic;
import com.server.sensor_log.application.usecases.ReceiveMetricsDataUseCase;
import com.server.sensor_log.application.workers.ReconnectionWorker;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class MqttConnectionManager {

    private final ReconnectionWorker reconnectionWorker;
    private final ReceiveMetricsDataUseCase receiveMetricsDataUseCase;
    private final Map<String, Consumer<Mqtt5Publish>> subscriptions = new ConcurrentHashMap<>();

    @Autowired
    private Mqtt5AsyncClient mqttClient;

    @Value("${mqtt.host}")
    @NotBlank
    private String host;

    @Value("${mqtt.port}")
    @Min(1)
    @Max(65535)
    private int port;

    @Value("${mqtt.client-id}")
    @NotBlank
    private String clientId;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Value("${mqtt.ssl-enabled:false}")
    private boolean sslEnabled;

    @Value("${mqtt.keystore-path:}")
    private String keystorePath;

    @Value("${mqtt.keystore-password:}")
    private String keystorePassword;

    private String topic = "iot/+/data";

    public MqttConnectionManager(
            ReconnectionWorker reconnectionWorker,
            ReceiveMetricsDataUseCase receiveMetricsDataUseCase) {
        this.reconnectionWorker = reconnectionWorker;
        this.receiveMetricsDataUseCase = receiveMetricsDataUseCase;
    }

    @Bean
    public Mqtt5AsyncClient mqttClient() throws Exception {
        var builder = MqttClient.builder()
                .useMqttVersion5()
                .identifier(clientId)
                .serverHost(host)
                .serverPort(port)
                .automaticReconnect(MqttClientAutoReconnect.builder()
                        .initialDelay(500, TimeUnit.MILLISECONDS)
                        .maxDelay(10, TimeUnit.SECONDS)
                        .build());

        if (sslEnabled) {
            builder = builder.sslConfig()
                    .keyManagerFactory(buildKeyManagerFactory())
                    .trustManagerFactory(buildTrustManagerFactory())
                    .applySslConfig();
        }
        if (!username.isBlank()) {
            builder = builder.simpleAuth()
                    .username(username)
                    .password(password.getBytes(StandardCharsets.UTF_8))
                    .applySimpleAuth();
        }

        log.info("MQTT config: host={}, port={}, clientId={}, user={}, ssl={}",
                host, port, clientId, username, sslEnabled);
        return builder.buildAsync();
    }

    @PostConstruct
    public void start() {
        log.info("Connecting to MQTT broker...");
        mqttClient.connect()
                .thenCompose(connAck -> {
                    log.info("Connected successfully to MQTT broker");
                    return subscribe(topic, this::onMessages);
                })
                .exceptionally(ex -> {
                    log.warn("MQTT connection failed, scheduling reconnection...", ex);
                    reconnectionWorker.scheduleReconnect(this::tryReconnect);
                    return null;
                });
    }

    public void publish(String topic, String payload) {
        mqttClient.publishWith()
                .topic(topic)
                .payload(payload.getBytes(StandardCharsets.UTF_8))
                .qos(MqttQos.AT_LEAST_ONCE)
                .send()
                .thenRun(() -> log.info("Message published to topic: {}", topic))
                .exceptionally(throwable -> {
                    log.error("Failed to publish message to topic: {}", topic, throwable);
                    return null;
                });
    }

    public void subscribe(String topic) {
        this.topic = topic;
        subscribe(topic, this::onMessages);
    }

    public String getTopic() {
        return topic;
    }

    @PreDestroy
    public void stop() {
        log.info("Disconnecting from MQTT broker...");
        disconnect();
    }

    public void onMessages(@NonNull Mqtt5Publish mqttMessage) {
        String receivedTopic = mqttMessage.getTopic().toString();
        String payload = new String(mqttMessage.getPayloadAsBytes(), StandardCharsets.UTF_8);

        if (receivedTopic.isBlank() || payload.isBlank()) {
            log.error("MQTT message rejected because topic or payload is blank");
            return;
        }

        try {
            Payload payloadObject = Payload.parse(payload);
            Topic topicObject = Topic.parse(receivedTopic);
            receiveMetricsDataUseCase.execute(topicObject, payloadObject);
        } catch (Exception exception) {
            log.error("Failed to handle MQTT message for topic: {}", receivedTopic, exception);
        }
    }

    private CompletableFuture<Void> subscribe(String topic, Consumer<Mqtt5Publish> callback) {
        if (!mqttClient.getState().isConnected()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Cannot subscribe because MQTT client is not connected"));
        }
        if (subscriptions.containsKey(topic)) {
            return CompletableFuture.completedFuture(null);
        }

        return mqttClient.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(callback)
                .send()
                .thenRun(() -> subscriptions.putIfAbsent(topic, callback))
                .exceptionally(throwable -> {
                    subscriptions.remove(topic);
                    tryUnsubscribeFromBroker(topic);
                    throw new CompletionException("MQTT subscribe failed", throwable);
                });
    }

    private CompletableFuture<Void> disconnect() {
        if (!mqttClient.getState().isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        subscriptions.keySet().forEach(this::tryUnsubscribeFromBroker);
        subscriptions.clear();
        return mqttClient.disconnect();
    }

    private void tryReconnect() {
        try {
            if (mqttClient.getState() == MqttClientState.CONNECTING
                    || mqttClient.getState().isConnected()) {
                mqttClient.disconnect().join();
            }
            mqttClient.connect().orTimeout(5, TimeUnit.SECONDS).join();
            resubscribeAll();
            reconnectionWorker.cancelReconnect();
            log.info("MQTT reconnected successfully");
        } catch (CompletionException exception) {
            log.error("Error while reconnecting to MQTT", exception.getCause());
        }
    }

    private void resubscribeAll() {
        Set<String> topics = Set.copyOf(subscriptions.keySet());
        topics.forEach(subscription -> {
            Consumer<Mqtt5Publish> callback = subscriptions.get(subscription);
            subscriptions.remove(subscription);
            subscribe(subscription, callback);
        });
    }

    private void tryUnsubscribeFromBroker(String topic) {
        try {
            mqttClient.unsubscribeWith().topicFilter(topic).send().get(5, TimeUnit.SECONDS);
            subscriptions.remove(topic);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while unsubscribing from topic: {}", topic, exception);
        } catch (ExecutionException | TimeoutException exception) {
            log.warn("Could not unsubscribe from topic: {}", topic, exception);
        }
    }

    private KeyManagerFactory buildKeyManagerFactory() throws Exception {
        KeyStore keyStore = loadKeyStore();
        KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        factory.init(keyStore, keystorePassword.toCharArray());
        return factory;
    }

    private TrustManagerFactory buildTrustManagerFactory() throws Exception {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        factory.init(loadKeyStore());
        return factory;
    }

    private KeyStore loadKeyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream input = Files.newInputStream(Path.of(keystorePath))) {
            keyStore.load(input, keystorePassword.toCharArray());
        }
        return keyStore;
    }
}
