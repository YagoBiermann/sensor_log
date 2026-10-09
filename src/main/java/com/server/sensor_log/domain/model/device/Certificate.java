package com.server.sensor_log.domain.model.device;

import java.time.Instant;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;

public record Certificate(String certificatePEM, String certificateSerialNumber, Instant certificateExpiresAt,
        Instant certificateIssuedAt) {

    public static String toPem(X509Certificate certificate) throws CertificateEncodingException {
        Base64.Encoder encoder = Base64.getMimeEncoder(64, "\n".getBytes());
        String encoded = encoder.encodeToString(certificate.getEncoded());
        return "-----BEGIN CERTIFICATE-----\n" + encoded + "\n-----END CERTIFICATE-----";
    }
}
