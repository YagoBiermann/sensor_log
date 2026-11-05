package com.server.sensor_log.application.usecases;

import org.springframework.stereotype.Component;

import com.server.sensor_log.application.exceptions.DeviceNotFoundException;
import com.server.sensor_log.application.services.AuthenticationService;
import com.server.sensor_log.application.services.MqttDynamicSecurityService;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.infra.repository.DeviceRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RevokeDeviceUseCase {
    private final DeviceRepository deviceRepository;
    private final AuthenticationService authenticationService;
    private final MqttDynamicSecurityService mqttDynamicSecurityService;

    public void execute(String serialNumber, String userId, String role)
            throws DeviceNotFoundException, SecurityException {
        Device device = deviceRepository.findById(serialNumber)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found"));
        authenticationService.authorizeOwnerOrAdmin(device.getOwnerId(), userId, role);

        if (device.getCertificate() == null) {
            throw new SecurityException("Device has no certificate to revoke");
        }

        authenticationService.revokeCertificate(device.getCertificate().certificateSerialNumber());
        mqttDynamicSecurityService.revokeDevice(serialNumber).join();
        device.revoke();
        deviceRepository.save(device);
    }

}
