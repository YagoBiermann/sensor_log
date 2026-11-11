package com.server.sensor_log.application.usecases;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.springframework.stereotype.Component;

import com.server.sensor_log.application.controllers.device.dto.CertificateResponse;
import com.server.sensor_log.application.exceptions.DeviceNotFoundException;
import com.server.sensor_log.application.exceptions.InvalidDeviceStatusException;
import com.server.sensor_log.application.services.AuthenticationService;
import com.server.sensor_log.domain.model.device.Certificate;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.infra.repository.DeviceRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeviceEnrollmentUseCase {
    private final DeviceRepository deviceRepository;
    private final AuthenticationService authenticationService;

    public CertificateResponse execute(String serialNumber, String csr)
            throws DeviceNotFoundException, CertificateEncodingException {
        Device device = deviceRepository.findById(serialNumber)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found"));

        if (device.isRevoked()) {
            throw new InvalidDeviceStatusException("Device is revoked and cannot be enrolled");
        }

        if (!device.isClaimed()) {
            throw new InvalidDeviceStatusException("Device must be claimed before enrollment");
        }

        if (device.hasCertificate()) {
            throw new InvalidDeviceStatusException("Device is already enrolled");
        }

        X509Certificate signedCertificate = authenticationService.signCertificate(serialNumber, csr);
        Certificate certificate = new Certificate(
                Certificate.toPem(signedCertificate),
                signedCertificate.getSerialNumber().toString(),
                signedCertificate.getNotAfter().toInstant(),
                signedCertificate.getNotBefore().toInstant());

        device.enroll(certificate);
        deviceRepository.save(device);

        return new CertificateResponse(
                certificate.certificatePEM(),
                certificate.certificateSerialNumber(),
                certificate.certificateExpiresAt().toString());
    }
}
