package com.server.sensor_log.application.ports;

import com.server.sensor_log.domain.model.device.Device;

public interface DeviceRepositoryPort {
    void save(Device device);
}
