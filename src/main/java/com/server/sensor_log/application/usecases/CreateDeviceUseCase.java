package com.server.sensor_log.application.usecases;

import org.springframework.stereotype.Component;

import com.server.sensor_log.application.dto.DeviceDTO;
import com.server.sensor_log.application.mappers.DeviceMapper;
import com.server.sensor_log.application.ports.DeviceRepositoryPort;
import com.server.sensor_log.domain.model.device.Device;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CreateDeviceUseCase {
    private final DeviceMapper deviceMapper;
    private final DeviceRepositoryPort deviceRepositoryPort;
    public CreateDeviceUseCase(DeviceMapper deviceMapper, DeviceRepositoryPort deviceRepositoryPort) {
        this.deviceMapper = deviceMapper;
        this.deviceRepositoryPort = deviceRepositoryPort;
    }

    public void execute(DeviceDTO deviceDTO){
        boolean deviceExists = deviceRepositoryPort.findById(deviceDTO.getDeviceId()).isPresent();
        if(deviceExists) {
            log.warn("🟠 Device already exists: {}", deviceDTO.getDeviceId());
            throw new IllegalArgumentException("Device already exists");
        }
        log.info("🔵 Creating new device: {}", deviceDTO);
        Device device = deviceMapper.toEntity(deviceDTO);
        log.debug("🔵 Mapped deviceDTO into Device entity: {}", device);
        deviceRepositoryPort.save(device);
        log.info("🟢 saved device entity: {}", device);
    }
}
