package com.server.sensor_log.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.server.sensor_log.documents.device.Sensor;

public interface SensorRepository extends MongoRepository<Sensor, String> {
    long countById(String id);
}
