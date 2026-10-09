package com.server.sensor_log.application.usecases;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

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

    public ClaimDeviceResponse execute(ClaimDeviceRequest request) throws SecurityException, DeviceNotFoundException, CertificateEncodingException {
        Device device = deviceRepository.findById(request.serialNumber())
                .orElseThrow(() -> new DeviceNotFoundException("Device not found"));

        X509Certificate x509certificate = authService.signCertificate(request);
        Certificate certificate = new Certificate(Certificate.toPem(x509certificate),
                x509certificate.getSerialNumber().toString(), x509certificate.getNotAfter().toInstant(),
                x509certificate.getNotBefore().toInstant());
        device.claim(request.ownerId(), request.redeemToken(), certificate, passwordEncoder);
        deviceRepository.save(device);

        return new ClaimDeviceResponse(device.getSerialNumber(), device.getOwnerId(), device.getClaimStatus(), certificate);
    }
}
