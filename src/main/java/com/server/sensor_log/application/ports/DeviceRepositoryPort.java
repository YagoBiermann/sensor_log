package com.server.sensor_log.application.ports;

import com.server.sensor_log.domain.model.device.Device;

import java.util.Optional;

public interface DeviceRepositoryPort {
    void save(Device device);
    Optional<Device> findById(String deviceId);
}
