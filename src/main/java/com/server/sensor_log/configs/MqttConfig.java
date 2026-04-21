package com.server.sensor_log.configs;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MqttProperties.class)
@Profile("!test")
public class MqttConfig {

    private final MqttProperties props;

    @Bean
    public Mqtt5AsyncClient mqttClient() {
        Mqtt5ClientBuilder builder = MqttClient.builder()
                .useMqttVersion5()
                .identifier(props.getClientId())
                .serverHost(props.getHost())
                .serverPort(props.getPort())
                .automaticReconnectWithDefaultConfig();

        System.out.println("MQTT Config - Host: " + props.getHost() + ", Port: " + props.getPort() + ", Client ID: " + props.getClientId());
        if (props.getUsername() != null) {
            builder.simpleAuth()
                    .username(props.getUsername())
                    .password(props.getPassword().getBytes())
                    .applySimpleAuth();
        }

        if (props.isSslEnabled()) {
            builder.sslWithDefaultConfig();
        }

        return builder.buildAsync();
    }
}