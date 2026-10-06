package com.server.sensor_log.application.usecases;

import org.springframework.stereotype.Component;

import com.server.sensor_log.application.controllers.dto.CreateDeviceResponse;
import com.server.sensor_log.application.usecases.dto.NewDevice;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.RandomGenerator;
import com.server.sensor_log.infra.repository.DeviceRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CreateDeviceUseCase {
    private final DeviceRepository deviceRepository;
    private final RandomGenerator identityGenerator;

    public CreateDeviceUseCase(DeviceRepository deviceRepository,
            RandomGenerator identityGenerator) {
        this.identityGenerator = identityGenerator;
        this.deviceRepository = deviceRepository;
    }

    public CreateDeviceResponse execute(NewDevice newDevice) {
        String serialNumber = identityGenerator.generateSerialNumber();
        String bootstrapToken = identityGenerator.generateBootstrapToken();
        while (deviceRepository.existsById(serialNumber)) {
            serialNumber = identityGenerator.generateSerialNumber();
        }
        String serialNumberHash = identityGenerator.encode(serialNumber);
        String bootstrapTokenHash = identityGenerator.encode(bootstrapToken);

        Device device = Device.create(serialNumberHash, bootstrapTokenHash, newDevice.description(), newDevice.location(), newDevice.deviceMetrics());
        log.info("🔵 Creating new device: {}", newDevice);
        deviceRepository.save(device);
        log.info("🟢 saved device entity: {}", device);

        return new CreateDeviceResponse(serialNumber, bootstrapToken);
    }
}
