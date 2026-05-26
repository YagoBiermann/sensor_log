package com.server.sensor_log.infra.repository;

import com.server.sensor_log.domain.model.device.device_readings.LightReading;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LightDataReadingRepository extends MongoRepository<LightReading, String> {
}
