package com.server.sensor_log.application.usecases;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.server.sensor_log.application.controllers.device.dto.BootDeviceResponse;
import com.server.sensor_log.application.controllers.device.dto.FirstBootRequest;
import com.server.sensor_log.application.exceptions.DeviceNotFoundException;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.RandomGenerator;
import com.server.sensor_log.infra.repository.DeviceRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class BootDeviceUseCase {
    private final DeviceRepository deviceRepository;
    private final RandomGenerator identityGenerator;

    public BootDeviceResponse execute(FirstBootRequest firstBootRequest)
            throws SecurityException, DeviceNotFoundException {
        String serialNumber = identityGenerator.encode(firstBootRequest.serialNumber());
        String firstBootToken = identityGenerator.encode(firstBootRequest.firstBootToken());
        Device device = deviceRepository.findById(serialNumber)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found"));
        device.boot(serialNumber, firstBootToken, identityGenerator);

        return new BootDeviceResponse(device.getRedeemToken());
    }
}
