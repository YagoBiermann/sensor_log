package com.server.sensor_log.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.server.sensor_log.application.controllers.device.dto.ClaimDeviceRequest;
import com.server.sensor_log.application.exceptions.DeviceNotFoundException;
import com.server.sensor_log.application.exceptions.ExpiredCodeException;
import com.server.sensor_log.application.exceptions.InvalidCodeException;
import com.server.sensor_log.application.exceptions.MaxAttemptsFailedException;
import com.server.sensor_log.application.exceptions.RetryBeforeCooldownException;
import com.server.sensor_log.application.services.AuthenticationService;
import com.server.sensor_log.domain.model.device.ClaimStatus;
import com.server.sensor_log.domain.model.device.ConnectionStatus;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.MetricsType;
import com.server.sensor_log.domain.model.device.RandomGenerator;
import com.server.sensor_log.domain.model.device.RedeemCode;
import com.server.sensor_log.infra.repository.DeviceRepository;

@ExtendWith(MockitoExtension.class)
class ClaimDeviceUseCaseTest {
    private static final String SERIAL = "123456789012";
    private static final String OWNER = "owner-1";
    private static final String CSR =
            "-----BEGIN CERTIFICATE REQUEST-----\nYWJj\n-----END CERTIFICATE REQUEST-----";

    @Mock DeviceRepository repository;
    @Mock PasswordEncoder encoder;
    @Mock RandomGenerator generator;
    @Mock AuthenticationService authenticationService;

    @Test
    void rejectsUnknownDevice() {
        when(repository.findById(SERIAL)).thenReturn(Optional.empty());

        assertThrows(DeviceNotFoundException.class, () -> useCase().execute(request("redeem")));

        verifyNoInteractions(encoder, generator, authenticationService);
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void claimsPendingDeviceAndClearsRedeemCode() {
        Device device = pendingDevice(validCode("redeem", 0, Instant.EPOCH));
        when(repository.findById(SERIAL)).thenReturn(Optional.of(device));

        var response = useCase().execute(request("redeem"));

        assertEquals(SERIAL, response.serialNumber());
        assertEquals(OWNER, response.ownerId());
        assertEquals(ClaimStatus.CLAIMED, response.claimStatus());
        assertEquals(ClaimStatus.CLAIMED, device.getClaimStatus());
        assertEquals(ConnectionStatus.ONLINE, device.getConnectionStatus());
        assertEquals(OWNER, device.getOwnerId());
        assertNotNull(device.getClaimedAt());
        assertNull(device.getRedeemCode());
        assertNull(device.getCertificate());
        verify(repository).save(device);
        verifyNoInteractions(encoder, generator, authenticationService);
    }

    @Test
    void rejectsClaimBeforeBootAndForClaimedOrRevokedDevices() {
        Device unclaimed = Device.create(SERIAL, "bootstrap", "", null, java.util.List.of(MetricsType.TEMPERATURE));
        Device claimed = claimedDevice();
        Device revoked = Device.restore(SERIAL, OWNER, null, Instant.now(), ClaimStatus.REVOKED,
                ConnectionStatus.OFFLINE, null, "", Map.of(MetricsType.TEMPERATURE, 0.0),
                "bootstrap", null, Instant.now());
        when(repository.findById(SERIAL)).thenReturn(
                Optional.of(unclaimed), Optional.of(claimed), Optional.of(revoked));

        assertThrows(SecurityException.class, () -> useCase().execute(request("redeem")));
        assertThrows(SecurityException.class, () -> useCase().execute(request("redeem")));
        assertThrows(SecurityException.class, () -> useCase().execute(request("redeem")));

        verify(repository, times(3)).findById(SERIAL);
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(encoder, generator, authenticationService);
    }

    @Test
    void recordsInvalidAttemptsAndAppliesCooldownAtTheLimit() {
        Device device = pendingDevice(validCode("redeem", 0, Instant.EPOCH));
        when(repository.findById(SERIAL)).thenReturn(Optional.of(device));

        for (int attempt = 1; attempt < RedeemCode.MAX_FAILED_ATTEMPTS; attempt++) {
            assertThrows(InvalidCodeException.class, () -> useCase().execute(request("wrong")));
        }
        assertThrows(MaxAttemptsFailedException.class, () -> useCase().execute(request("wrong")));

        assertTrue(device.getRedeemCode().isInCooldown());
        assertEquals(ClaimStatus.PENDING, device.getClaimStatus());
        verify(repository, times(RedeemCode.MAX_FAILED_ATTEMPTS)).save(device);
    }

    @Test
    void blocksAttemptsDuringCooldownAndPersistsThatState() {
        Device device = pendingDevice(validCode(
                "redeem", RedeemCode.MAX_FAILED_ATTEMPTS, Instant.now().plusSeconds(30)));
        when(repository.findById(SERIAL)).thenReturn(Optional.of(device));

        assertThrows(RetryBeforeCooldownException.class, () -> useCase().execute(request("redeem")));

        assertEquals(ClaimStatus.PENDING, device.getClaimStatus());
        assertTrue(device.getRedeemCode().isInCooldown());
        verify(repository).save(device);
    }

    @Test
    void rejectsExpiredRedeemCodeWithoutSavingOrClaiming() {
        Device device = pendingDevice(validCode("redeem", 0, Instant.EPOCH,
                Instant.now().minusSeconds(1)));
        when(repository.findById(SERIAL)).thenReturn(Optional.of(device));

        assertThrows(ExpiredCodeException.class, () -> useCase().execute(request("redeem")));

        assertEquals(ClaimStatus.PENDING, device.getClaimStatus());
        assertNull(device.getOwnerId());
        verify(repository, never()).save(device);
    }

    @Test
    void permitsCorrectCodeAfterCooldownEnds() {
        Device device = pendingDevice(validCode(
                "redeem", RedeemCode.MAX_FAILED_ATTEMPTS, Instant.now().minusSeconds(31)));
        when(repository.findById(SERIAL)).thenReturn(Optional.of(device));

        var response = useCase().execute(request("redeem"));

        assertEquals(ClaimStatus.CLAIMED, response.claimStatus());
        assertEquals(OWNER, device.getOwnerId());
        assertNull(device.getRedeemCode());
        verify(repository).save(device);
    }

    private ClaimDeviceUseCase useCase() {
        return new ClaimDeviceUseCase(repository, encoder, generator, authenticationService);
    }

    private ClaimDeviceRequest request(String redeemCode) {
        return new ClaimDeviceRequest(SERIAL, OWNER, redeemCode, CSR);
    }

    private Device pendingDevice(RedeemCode redeemCode) {
        return Device.restore(SERIAL, null, null, null, ClaimStatus.PENDING, ConnectionStatus.ONLINE,
                null, "", Map.of(MetricsType.TEMPERATURE, 0.0), "bootstrap", redeemCode, Instant.now());
    }

    private Device claimedDevice() {
        return Device.restore(SERIAL, OWNER, null, Instant.now(), ClaimStatus.CLAIMED, ConnectionStatus.ONLINE,
                null, "", Map.of(MetricsType.TEMPERATURE, 0.0), "bootstrap", null, Instant.now());
    }

    private RedeemCode validCode(String code, int attempts, Instant cooldownUntil) {
        return validCode(code, attempts, cooldownUntil, Instant.now().plusSeconds(60));
    }

    private RedeemCode validCode(String code, int attempts, Instant cooldownUntil, Instant expiresAt) {
        return RedeemCode.restore(code, expiresAt, attempts, cooldownUntil, generator);
    }
}
