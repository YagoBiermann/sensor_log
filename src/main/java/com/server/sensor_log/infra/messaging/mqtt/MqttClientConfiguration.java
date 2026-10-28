package com.server.sensor_log.infra.messaging.mqtt;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.lifecycle.MqttClientAutoReconnect;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class MqttClientConfiguration {

    @Value("${mqtt.host}")
    private String host;

    @Value("${mqtt.port}")
    private int port;

    @Value("${mqtt.client-id}")
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

    private KeyManagerFactory buildKeyManagerFactory() throws Exception {
        KeyManagerFactory factory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        factory.init(loadKeyStore(), keystorePassword.toCharArray());
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
