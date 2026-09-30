package com.server.sensor_log.application.controllers;

import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.server.sensor_log.application.controllers.dto.CertificateResponse;
import com.server.sensor_log.application.controllers.dto.SignCertificateRequest;
import com.server.sensor_log.application.services.VaultService;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.infra.repository.DeviceRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/device")
public class AuthController {
    private final VaultService vaultService;
    private final DeviceRepository deviceRepository;

    public AuthController(VaultService vaultService, DeviceRepository deviceRepository) {
        this.vaultService = vaultService;
        this.deviceRepository = deviceRepository;
    }

    @PostMapping(value = "/sign-request", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> signRequest(@Valid @RequestBody SignCertificateRequest signRequest)
            throws CertificateEncodingException {
        try {
            boolean deviceExists = deviceRepository.existsById(signRequest.serialNumber());
            if (!deviceExists) {
                return ResponseEntity.badRequest()
                        .body("Device with serial number " + signRequest.serialNumber() + " does not exist.");
            }
            Device device = deviceRepository.findById(signRequest.serialNumber())
                    .orElseThrow(() -> new RuntimeException("Device not found"));


            device.claim(signRequest.ownerId(), signRequest.bootstrapToken(), signRequest.redeemToken());

            X509Certificate signedCertificate = vaultService.signCertificate(signRequest);
            String serialNumber = signedCertificate.getSerialNumber().toString();
            String expiresAt = signedCertificate.getNotAfter().toInstant().toString();
    
            deviceRepository.save(device);

            CertificateResponse response = new CertificateResponse(
                    toPem(signedCertificate),
                    serialNumber,
                    expiresAt);
    
            return ResponseEntity.ok(response);
        } catch (CertificateEncodingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    private String toPem(Certificate certificate) throws CertificateEncodingException {
        Base64.Encoder encoder = Base64.getMimeEncoder(64, "\n".getBytes());
        String encoded = encoder.encodeToString(certificate.getEncoded());
        return "-----BEGIN CERTIFICATE-----\n" + encoded + "\n-----END CERTIFICATE-----";
    }
}
