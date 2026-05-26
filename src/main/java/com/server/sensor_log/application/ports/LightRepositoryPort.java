package com.server.sensor_log.application.ports;

import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.device_readings.LightReading;

import java.util.Optional;

public interface LightRepositoryPort {
    void save(LightReading lightReading);
    Optional<Device> findById(String id);
}
