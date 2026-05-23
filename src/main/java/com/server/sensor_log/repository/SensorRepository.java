package com.server.sensor_log.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.server.sensor_log.documents.device.Device;

public interface SensorRepository extends MongoRepository<Device, String> {
    long countById(String id);
}
