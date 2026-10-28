package com.server.sensor_log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class SensorLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(SensorLogApplication.class, args);
    }
}
