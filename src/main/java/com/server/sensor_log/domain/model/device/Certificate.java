package com.server.sensor_log.domain.model.device;

import java.time.Instant;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;

public record Certificate(String certificatePEM, String certificateSerialNumber, Instant certificateExpiresAt,
        Instant certificateIssuedAt) {

}
