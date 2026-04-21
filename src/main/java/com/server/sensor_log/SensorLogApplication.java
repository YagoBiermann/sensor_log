package com.server.sensor_log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.server.sensor_log.configs.MqttProperties;

@SpringBootApplication
@EnableConfigurationProperties(MqttProperties.class)
public class SensorLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(SensorLogApplication.class, args);
    }
}
