package com.server.sensor_log.application.usecases;

import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.server.sensor_log.application.controllers.device.dto.CreateDeviceResponse;
import com.server.sensor_log.application.controllers.device.dto.NewDevice;
import com.server.sensor_log.application.services.JwtService;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.Location;
import com.server.sensor_log.domain.model.device.RandomGenerator;
import com.server.sensor_log.domain.model.device.Role;
import com.server.sensor_log.infra.repository.DeviceRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CreateDeviceUseCase {
    private final DeviceRepository deviceRepository;
    private final RandomGenerator identityGenerator;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public CreateDeviceUseCase(DeviceRepository deviceRepository,
            RandomGenerator identityGenerator, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.identityGenerator = identityGenerator;
        this.deviceRepository = deviceRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public CreateDeviceResponse execute(NewDevice newDevice) {
        Objects.requireNonNull(newDevice, "Device data must be provided");
        if (newDevice.deviceMetrics() == null || newDevice.deviceMetrics().isEmpty()) {
            throw new IllegalArgumentException("At least one device metric type must be provided");
        }
        String serialNumber = identityGenerator.generateSerialNumber();
        while (deviceRepository.existsById(serialNumber)) {
            serialNumber = identityGenerator.generateSerialNumber();
        }
        String bootstrapToken = jwtService.generateToken(serialNumber, Role.DEVICE.name());
        String bootstrapTokenHash = passwordEncoder.encode(bootstrapToken);

        Device device = Device.create(serialNumber, bootstrapTokenHash, "", new Location(0, 0), newDevice.deviceMetrics());
        log.info("Creating new device with serial {}", serialNumber);
        deviceRepository.save(device);
        log.info("Device {} saved", serialNumber);

        return new CreateDeviceResponse(serialNumber, bootstrapToken);
    }
}
