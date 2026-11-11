package com.server.sensor_log.application.usecases;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.server.sensor_log.application.controllers.device.dto.ClaimDeviceRequest;
import com.server.sensor_log.application.controllers.device.dto.ClaimDeviceResponse;
import com.server.sensor_log.application.exceptions.DeviceNotFoundException;
import com.server.sensor_log.application.services.AuthenticationService;
import com.server.sensor_log.domain.model.device.Certificate;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.infra.repository.DeviceRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ClaimDeviceUseCase {
    private DeviceRepository deviceRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationService authService;

    public ClaimDeviceResponse execute(ClaimDeviceRequest request) throws SecurityException, DeviceNotFoundException {
        Device device = deviceRepository.findById(request.serialNumber())
                .orElseThrow(() -> new DeviceNotFoundException("Device not found"));

        if (device.isClaimed()) {
            throw new SecurityException("Device is already claimed");
        }
        if(!passwordEncoder.matches(request.redeemCode(), device.getRedeemCode())) {
            throw new SecurityException("Invalid redeem code");
        }

        device.claim(request.ownerId(), request.redeemCode(), passwordEncoder);
        deviceRepository.save(device);

        return new ClaimDeviceResponse(device.getSerialNumber(), device.getOwnerId(), device.getClaimStatus());
    }
}
