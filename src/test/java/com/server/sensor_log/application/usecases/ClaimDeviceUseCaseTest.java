package com.server.sensor_log.application.usecases;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.server.sensor_log.application.controllers.device.dto.ClaimDeviceRequest;
import com.server.sensor_log.application.exceptions.DeviceNotFoundException;
import com.server.sensor_log.application.services.AuthenticationService;
import com.server.sensor_log.domain.model.device.ClaimStatus;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.MetricsType;
import com.server.sensor_log.infra.repository.DeviceRepository;

@ExtendWith(MockitoExtension.class)
class ClaimDeviceUseCaseTest {
    @Mock DeviceRepository repository;
    @Mock PasswordEncoder encoder;
    @Mock AuthenticationService authenticationService;

    @Test
    void rejectsUnknownDevice() {
        when(repository.findById("123456789012")).thenReturn(Optional.empty());

        assertThrows(DeviceNotFoundException.class, () -> new ClaimDeviceUseCase(repository, encoder, authenticationService)
                .execute(new ClaimDeviceRequest("123456789012", "owner", "redeem", "csr")));
        verifyNoInteractions(authenticationService);
    }

    @Test
    void rejectsInvalidRedeemCodeAndAlreadyClaimedDevice() {
        Device unclaimed = Device.restore("123456789012", null, null, null, ClaimStatus.PENDING,
                com.server.sensor_log.domain.model.device.ConnectionStatus.ONLINE, null, "",
                java.util.Map.of(MetricsType.TEMPERATURE, 0.0), "", "hash", java.time.Instant.now());
        when(repository.findById("123456789012")).thenReturn(Optional.of(unclaimed));
        when(encoder.matches("wrong", "hash")).thenReturn(false);

        assertThrows(SecurityException.class, () -> new ClaimDeviceUseCase(repository, encoder, authenticationService)
                .execute(new ClaimDeviceRequest("123456789012", "owner", "wrong", "csr")));
        verifyNoInteractions(authenticationService);

        Device claimed = Device.restore("123456789012", "owner", null, java.time.Instant.now(), ClaimStatus.CLAIMED,
                com.server.sensor_log.domain.model.device.ConnectionStatus.ONLINE, null, "",
                java.util.Map.of(MetricsType.TEMPERATURE, 0.0), "", "hash", java.time.Instant.now());
        when(repository.findById("123456789012")).thenReturn(Optional.of(claimed));
        assertThrows(SecurityException.class, () -> new ClaimDeviceUseCase(repository, encoder, authenticationService)
                .execute(new ClaimDeviceRequest("123456789012", "owner", "redeem", "csr")));
    }

    @Test
    void claimsDeviceSuccessfullyWithValidRedeemCodeWithoutIssuingCertificate() throws Exception {
        Instant issuedAt = Instant.parse("2026-01-01T00:00:00Z");
        Device device = Device.restore("123456789012", null, null, null, ClaimStatus.PENDING,
                com.server.sensor_log.domain.model.device.ConnectionStatus.ONLINE, null, "",
                java.util.Map.of(MetricsType.TEMPERATURE, 0.0), "", "redeem-hash", issuedAt);
        ClaimDeviceRequest request = new ClaimDeviceRequest(
                "123456789012",
                "owner-1",
                "redeem",
                "-----BEGIN CERTIFICATE REQUEST-----\nYWJj\n-----END CERTIFICATE REQUEST-----");

        when(repository.findById(request.serialNumber())).thenReturn(Optional.of(device));
        when(encoder.matches(request.redeemCode(), device.getRedeemCode())).thenReturn(true);
        var response = new ClaimDeviceUseCase(repository, encoder, authenticationService).execute(request);

        assertEquals("123456789012", response.serialNumber());
        assertEquals("owner-1", response.ownerId());
        assertEquals(ClaimStatus.CLAIMED, response.claimStatus());
        assertEquals("owner-1", device.getOwnerId());
        assertEquals("", device.getRedeemCode());
        assertNull(device.getCertificate());
        verifyNoInteractions(authenticationService);
        verify(repository).save(device);
    }
}
