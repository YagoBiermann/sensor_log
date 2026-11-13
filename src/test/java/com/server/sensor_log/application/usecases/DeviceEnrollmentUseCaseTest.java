package com.server.sensor_log.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import java.security.cert.X509Certificate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.server.sensor_log.application.exceptions.DeviceNotFoundException;
import com.server.sensor_log.application.exceptions.InvalidDeviceStatusException;
import com.server.sensor_log.application.services.AuthenticationService;
import com.server.sensor_log.domain.model.device.ClaimStatus;
import com.server.sensor_log.domain.model.device.ConnectionStatus;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.MetricsType;
import com.server.sensor_log.infra.repository.DeviceRepository;

@ExtendWith(MockitoExtension.class)
class DeviceEnrollmentUseCaseTest {
    private static final String SERIAL = "123456789012";
    private static final String CSR = "-----BEGIN CERTIFICATE REQUEST-----\nYWJj\n-----END CERTIFICATE REQUEST-----";

    @Mock
    DeviceRepository repository;
    @Mock
    AuthenticationService authenticationService;

    @Test
    void rejectsUnknownDevice() {
        when(repository.findById(SERIAL)).thenReturn(Optional.empty());

        assertThrows(DeviceNotFoundException.class, () -> useCase().execute(SERIAL, CSR));
        verifyNoInteractions(authenticationService);
    }

    @Test
    void rejectsDeviceThatIsNotClaimedWithoutSigningOrSaving() {
        Device device = device(ClaimStatus.PENDING);
        when(repository.findById(SERIAL)).thenReturn(Optional.of(device));

        assertThrows(InvalidDeviceStatusException.class, () -> useCase().execute(SERIAL, CSR));

        verifyNoInteractions(authenticationService);
        verify(repository, org.mockito.Mockito.never()).save(device);
    }

    @Test
    void rejectsRevokedDeviceWithoutSigningOrSaving() {
        Device device = device(ClaimStatus.REVOKED);
        when(repository.findById(SERIAL)).thenReturn(Optional.of(device));

        assertThrows(InvalidDeviceStatusException.class, () -> useCase().execute(SERIAL, CSR));

        verifyNoInteractions(authenticationService);
        verify(repository, org.mockito.Mockito.never()).save(device);
    }

    @Test
    void signsAndStoresCertificateForClaimedDevice() throws Exception {
        Device device = device(ClaimStatus.CLAIMED);
        Instant issuedAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant expiresAt = Instant.parse("2026-02-01T00:00:00Z");
        X509Certificate signedCertificate = mock(X509Certificate.class);
        when(repository.findById(SERIAL)).thenReturn(Optional.of(device));
        when(authenticationService.signCertificate(SERIAL, CSR)).thenReturn(signedCertificate);
        when(signedCertificate.getEncoded()).thenReturn(new byte[] { 1, 2, 3 });
        when(signedCertificate.getSerialNumber()).thenReturn(BigInteger.valueOf(42));
        when(signedCertificate.getNotBefore()).thenReturn(Date.from(issuedAt));
        when(signedCertificate.getNotAfter()).thenReturn(Date.from(expiresAt));

        var response = useCase().execute(SERIAL, CSR);

        assertEquals("-----BEGIN CERTIFICATE-----", response.certificate().substring(0, 27));
        assertEquals("42", response.serialNumber());
        assertEquals(expiresAt.toString(), response.expiresAt());
        assertEquals(response.certificate(), device.getCertificate().certificatePEM());
        assertEquals("42", device.getCertificate().certificateSerialNumber());
        verify(authenticationService).signCertificate(SERIAL, CSR);
        verify(repository).save(device);
    }

    private DeviceEnrollmentUseCase useCase() {
        return new DeviceEnrollmentUseCase(repository, authenticationService);
    }

    private Device device(ClaimStatus status) {
        return Device.restore(
                SERIAL,
                "owner",
                null,
                status == ClaimStatus.CLAIMED ? Instant.now() : null,
                status,
                ConnectionStatus.ONLINE,
                null,
                "",
                Map.of(MetricsType.TEMPERATURE, 0.0),
                "",
                null,
                Instant.now());
    }
}
