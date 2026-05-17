package com.server.sensor_log.configs;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Component
@Slf4j
public class MongoHealthLogger {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void checkConnection() {
        try {
            mongoTemplate.executeCommand("{ ping: 1 }");
            log.info("✅ MongoDB connection is healthy");
        } catch (Exception e) {
            log.error("❌ MongoDB failed to respond", e);
        }
    }
}
