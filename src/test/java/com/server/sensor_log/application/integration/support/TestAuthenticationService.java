package com.server.sensor_log.application.integration.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;

import com.server.sensor_log.application.services.AuthenticationService;
import com.server.sensor_log.domain.model.device.RandomGenerator;
import com.server.sensor_log.infra.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

final class TestAuthenticationService extends AuthenticationService {

    TestAuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RandomGenerator randomGenerator) {
        super(null, userRepository, passwordEncoder, null, randomGenerator);
    }

    @Override
    public X509Certificate signCertificate(String serialNumber, String csr) {
        Instant issuedAt = Instant.parse("2026-01-01T00:00:00Z");
        X509Certificate certificate = mock(X509Certificate.class);
        try {
            when(certificate.getEncoded()).thenReturn(new byte[] { 1, 2, 3 });
            when(certificate.getSerialNumber()).thenReturn(BigInteger.ONE);
            when(certificate.getNotBefore()).thenReturn(Date.from(issuedAt));
            when(certificate.getNotAfter()).thenReturn(Date.from(issuedAt.plusSeconds(86400)));
        } catch (Exception exception) {
            throw new AssertionError("Could not configure test certificate", exception);
        }
        return certificate;
    }
}
