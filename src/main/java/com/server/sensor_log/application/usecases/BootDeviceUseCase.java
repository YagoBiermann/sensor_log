package com.server.sensor_log.application.usecases;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.server.sensor_log.application.controllers.device.dto.FirstBootResponse;
import com.server.sensor_log.application.controllers.device.dto.FirstBootRequest;
import com.server.sensor_log.application.exceptions.DeviceNotFoundException;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.RandomGenerator;
import com.server.sensor_log.infra.repository.DeviceRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@AllArgsConstructor
public class BootDeviceUseCase {
    private final DeviceRepository deviceRepository;
    private final RandomGenerator codeGenerator;
    private final PasswordEncoder passwordEncoder;

    public FirstBootResponse execute(FirstBootRequest firstBootRequest)
            throws SecurityException, DeviceNotFoundException {
        String serialNumber = firstBootRequest.serialNumber();
        String firstBootToken = firstBootRequest.firstBootToken();
        Device device = deviceRepository.findById(serialNumber)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found"));

        device.boot(serialNumber, firstBootToken, codeGenerator, passwordEncoder);
        log.info("Device {} booted successfully.", serialNumber);

        return new FirstBootResponse(device.getRedeemToken());
    }
}
