package com.server.sensor_log.application.services;

import java.security.cert.X509Certificate;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultPkiOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.Certificate;
import org.springframework.vault.support.VaultCertificateRequest;
import org.springframework.vault.support.VaultSignCertificateRequestResponse;

import com.server.sensor_log.application.controllers.dto.SignCertificateRequest;

@Service
public class VaultService {
    private final VaultTemplate vaultTemplate;
    @Value("${VAULT_PKI_ROLE_NAME}")
    private String roleName;

    public VaultService(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    public X509Certificate signCertificate(SignCertificateRequest signRequest) {
        VaultPkiOperations pkiOperations = vaultTemplate.opsForPki("pki_int");
        VaultCertificateRequest certificateSignRequest = VaultCertificateRequest.builder()
                .commonName(signRequest.serial_number())
                .ttl(Duration.ofHours(24))
                .altNames(signRequest.allowed_domains())
                .build();
        VaultSignCertificateRequestResponse response = pkiOperations.signCertificateRequest(roleName,
                signRequest.csr(), certificateSignRequest);
        Certificate certificate = response.getRequiredData();
            certificate.getSerialNumber();
            
        return certificate.getX509Certificate();
    }
}
