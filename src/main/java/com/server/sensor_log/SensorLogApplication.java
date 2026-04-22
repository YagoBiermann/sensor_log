package com.server.sensor_log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.server.sensor_log.configs.MqttProperties;
import com.server.sensor_log.repository.SensorRepository;

@SpringBootApplication
@EnableConfigurationProperties(MqttProperties.class)
@EnableMongoRepositories
public class SensorLogApplication {
    @Autowired
    SensorRepository sensorRepository;

    public static void main(String[] args) {
        SpringApplication.run(SensorLogApplication.class, args);
    }
}
