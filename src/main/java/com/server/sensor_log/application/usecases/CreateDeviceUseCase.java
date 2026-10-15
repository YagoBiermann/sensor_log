package com.server.sensor_log.application.usecases;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.server.sensor_log.application.controllers.device.dto.CreateDeviceResponse;
import com.server.sensor_log.application.controllers.device.dto.NewDevice;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.Location;
import com.server.sensor_log.domain.model.device.RandomGenerator;
import com.server.sensor_log.infra.repository.DeviceRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CreateDeviceUseCase {
    private final DeviceRepository deviceRepository;
    private final RandomGenerator identityGenerator;
    private final PasswordEncoder passwordEncoder;

    public CreateDeviceUseCase(DeviceRepository deviceRepository,
            RandomGenerator identityGenerator, PasswordEncoder passwordEncoder) {
        this.identityGenerator = identityGenerator;
        this.deviceRepository = deviceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public CreateDeviceResponse execute(NewDevice newDevice) {
        String serialNumber = identityGenerator.generateSerialNumber();
        String bootstrapToken = identityGenerator.generateBootstrapToken();
        while (deviceRepository.existsById(serialNumber)) {
            serialNumber = identityGenerator.generateSerialNumber();
        }
        String serialNumberHash = passwordEncoder.encode(serialNumber);
        String bootstrapTokenHash = passwordEncoder.encode(bootstrapToken);

        Device device = Device.create(serialNumberHash, bootstrapTokenHash, "", new Location(0,0), newDevice.deviceMetrics());
        log.info("🔵 Creating new device: {}", newDevice);
        deviceRepository.save(device);
        log.info("🟢 saved device entity: {}", device);

        return new CreateDeviceResponse(serialNumber, bootstrapToken);
    }
}
