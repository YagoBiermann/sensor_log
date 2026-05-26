package com.server.sensor_log.infra.repository;

import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.device_readings.LightReading;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DeviceRepositoryAdapter {

    private final DeviceRepository deviceRepository;
    private final LightDataReadingRepository lightDataReadingRepository;

    public DeviceRepositoryAdapter(DeviceRepository deviceRepository, LightDataReadingRepository lightDataReadingRepository) {
        this.deviceRepository = deviceRepository;
        this.lightDataReadingRepository = lightDataReadingRepository;
    }

    public void save(LightReading lightReading) {
        lightDataReadingRepository.save(lightReading);
    }

    public void save(Device newDevice) {
        deviceRepository.save(newDevice);
    }

    public Optional<Device> findById(String id) {
        return deviceRepository.findById(id);
    }

    public long countById(String id) {
        return deviceRepository.countById(id);
    }
}
