package com.server.sensor_log.infra.messaging.mqtt.configs;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@EnableConfigurationProperties(MqttProperties.class)
public class MqttConfig {
    private final MqttProperties props;

    public MqttConfig(MqttProperties props) {
        this.props = props;
    }

    private KeyManagerFactory buildKeyManagerFactory() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream input = Files.newInputStream(Path.of(props.keystorePath))) {
            keyStore.load(input, props.keystorePassword.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, props.keystorePassword.toCharArray());
        return kmf;
    }

    private TrustManagerFactory buildTrustManagerFactory() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (InputStream input = Files.newInputStream(Path.of(props.keystorePath))) {
            trustStore.load(input, props.keystorePassword.toCharArray());
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        return tmf;
    }

    @Bean
    public Mqtt5AsyncClient mqttClient() throws Exception {
        Mqtt5ClientBuilder builder = MqttClient.builder()
                .useMqttVersion5()
                .identifier(props.clientId)
                .serverHost(props.host)
                .serverPort(props.port);

        if (props.sslEnabled) {
            try {
                builder = builder.sslConfig().keyManagerFactory(buildKeyManagerFactory())
                        .trustManagerFactory(buildTrustManagerFactory()).applySslConfig();
            } catch (Exception e) {
                SecurityException securityException = new SecurityException(
                        "Failed to set up SSL context for MQTT client", e);
                log.error("🔴 Error setting up SSL context for MQTT client", securityException);
            }
        }
        if (props.username != null) {
            builder = builder.simpleAuth()
                    .username(props.username)
                    .password(props.password.getBytes())
                    .applySimpleAuth();
        }
        log.info("🔵 MQTT Config → host: {}, port: {}, clientId: {}, user: {}, ssl: {}",
                props.host,
                props.port,
                props.clientId,
                props.username,
                props.sslEnabled);

        return builder.buildAsync();
    }
}
