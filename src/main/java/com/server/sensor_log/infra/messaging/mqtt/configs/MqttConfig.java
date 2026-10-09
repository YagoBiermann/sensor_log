package com.server.sensor_log.infra.messaging.mqtt.configs;

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

    @Bean
    public Mqtt5AsyncClient mqttClient() {
        Mqtt5ClientBuilder builder = MqttClient.builder()
                .useMqttVersion5()
                .identifier(props.clientId)
                .serverHost(props.host)
                .serverPort(props.port);

        if (props.username != null) {
            builder = builder.simpleAuth()
                    .username(props.username)
                    .password(props.password.getBytes())
                    .applySimpleAuth();
        }

        if (props.sslEnabled) {
            builder = builder.sslWithDefaultConfig();

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
