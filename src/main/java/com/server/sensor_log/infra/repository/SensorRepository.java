package com.server.sensor_log.infra.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.server.sensor_log.domain.model.device.Device;

public interface SensorRepository extends MongoRepository<Device, String> {
    long countById(String id);
}
