package com.server.sensor_log.mqtt.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "mqtt")
@Data
public class MqttProperties {

    private String host;
    private int port;
    private String clientId;
    private String username;
    private String password;
    private boolean sslEnabled;
    private String sslCertificateFile;
}
