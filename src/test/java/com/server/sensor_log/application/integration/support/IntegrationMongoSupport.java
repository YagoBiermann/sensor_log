package com.server.sensor_log.application.integration.support;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.server.sensor_log.infra.repository.DeviceRepository;
import com.server.sensor_log.infra.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.MongoDBContainer;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;

public final class IntegrationMongoSupport {

    public static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0");

    private static MongoClient mongoClient;
    private static DeviceRepository deviceRepository;
    private static UserRepository userRepository;

    public static void setUpRepositories() {
        mongoClient = MongoClients.create(MONGO.getReplicaSetUrl());
        MongoTemplate mongoTemplate = new MongoTemplate(
                new SimpleMongoClientDatabaseFactory(mongoClient, "sensor_log_integration"));
        MongoRepositoryFactory repositoryFactory = new MongoRepositoryFactory(mongoTemplate);
        deviceRepository = repositoryFactory.getRepository(DeviceRepository.class);
        userRepository = repositoryFactory.getRepository(UserRepository.class);
    }

    public static void closeMongoClient() {
        mongoClient.close();
    }

    public DeviceRepository deviceRepository() {
        return deviceRepository;
    }

    public UserRepository userRepository() {
        return userRepository;
    }

    public void clear() {
        deviceRepository.deleteAll();
        userRepository.deleteAll();
    }
}
