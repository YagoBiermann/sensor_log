package com.server.sensor_log.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.server.sensor_log.documents.Sensor;

public interface SensorRepository extends MongoRepository<Sensor, String> {

}
