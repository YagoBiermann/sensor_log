package com.server.sensor_log.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.server.sensor_log.application.exceptions.DeviceNotFoundException;
import com.server.sensor_log.application.services.AuthenticationService;
import com.server.sensor_log.application.services.MqttDynamicSecurityService;
import com.server.sensor_log.domain.model.device.Certificate;
import com.server.sensor_log.domain.model.device.ClaimStatus;
import com.server.sensor_log.domain.model.device.ConnectionStatus;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.MetricsType;
import com.server.sensor_log.infra.repository.DeviceRepository;

@ExtendWith(MockitoExtension.class)
class RevokeDeviceUseCaseTest {
    private static final String SERIAL_NUMBER = "123456789012";
    private static final String OWNER_ID = "owner-1";

    @Mock DeviceRepository repository;
    @Mock AuthenticationService authenticationService;
    @Mock MqttDynamicSecurityService mqttDynamicSecurityService;

    @Test
    void revokesCertificateDisablesBrokerClientAndSavesDevice() {
        Device device = claimedDevice();
        when(repository.findById(SERIAL_NUMBER)).thenReturn(Optional.of(device));
        when(mqttDynamicSecurityService.revokeDevice(SERIAL_NUMBER))
                .thenReturn(CompletableFuture.completedFuture(null));

        new RevokeDeviceUseCase(repository, authenticationService, mqttDynamicSecurityService)
                .execute(SERIAL_NUMBER, OWNER_ID, "USER");

        assertEquals(ClaimStatus.REVOKED, device.getClaimStatus());
        assertEquals(ConnectionStatus.OFFLINE, device.getConnectionStatus());
        verify(authenticationService).authorizeOwnerOrAdmin(OWNER_ID, OWNER_ID, "USER");
        verify(authenticationService).revokeCertificate("certificate-serial");
        verify(mqttDynamicSecurityService).revokeDevice(SERIAL_NUMBER);
        verify(repository).save(device);
    }

    @Test
    void revokesDeviceAsAdmin() {
        Device device = claimedDevice();
        when(repository.findById(SERIAL_NUMBER)).thenReturn(Optional.of(device));
        when(mqttDynamicSecurityService.revokeDevice(SERIAL_NUMBER))
                .thenReturn(CompletableFuture.completedFuture(null));

        new RevokeDeviceUseCase(repository, authenticationService, mqttDynamicSecurityService)
                .execute(SERIAL_NUMBER, "admin-1", "ADMIN");

        verify(authenticationService).authorizeOwnerOrAdmin(OWNER_ID, "admin-1", "ADMIN");
        verify(repository).save(device);
    }

    @Test
    void rejectsUnknownDevice() {
        when(repository.findById(SERIAL_NUMBER)).thenReturn(Optional.empty());

        assertThrows(DeviceNotFoundException.class, () -> new RevokeDeviceUseCase(
                repository, authenticationService, mqttDynamicSecurityService)
                .execute(SERIAL_NUMBER, OWNER_ID, "USER"));

        verifyNoInteractions(authenticationService, mqttDynamicSecurityService);
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsUnauthorizedUserBeforeRevokingCertificate() {
        Device device = claimedDevice();
        when(repository.findById(SERIAL_NUMBER)).thenReturn(Optional.of(device));
        doThrow(new SecurityException("User is not allowed to access this device"))
                .when(authenticationService).authorizeOwnerOrAdmin(OWNER_ID, "other-user", "USER");

        assertThrows(SecurityException.class, () -> new RevokeDeviceUseCase(
                repository, authenticationService, mqttDynamicSecurityService)
                .execute(SERIAL_NUMBER, "other-user", "USER"));

        verify(authenticationService, never()).revokeCertificate(any());
        verifyNoInteractions(mqttDynamicSecurityService);
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsDeviceWithoutCertificate() {
        Device device = Device.restore(
                SERIAL_NUMBER, OWNER_ID, null, Instant.now(), ClaimStatus.CLAIMED,
                ConnectionStatus.ONLINE, null, "", Map.of(MetricsType.TEMPERATURE, 0.0),
                "", "redeem-hash", Instant.now());
        when(repository.findById(SERIAL_NUMBER)).thenReturn(Optional.of(device));

        assertThrows(SecurityException.class, () -> new RevokeDeviceUseCase(
                repository, authenticationService, mqttDynamicSecurityService)
                .execute(SERIAL_NUMBER, OWNER_ID, "USER"));

        verify(authenticationService, never()).revokeCertificate(any());
        verifyNoInteractions(mqttDynamicSecurityService);
        verify(repository, never()).save(any());
    }

    @Test
    void doesNotPersistWhenBrokerRevocationFails() {
        Device device = claimedDevice();
        when(repository.findById(SERIAL_NUMBER)).thenReturn(Optional.of(device));
        when(mqttDynamicSecurityService.revokeDevice(SERIAL_NUMBER))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("broker unavailable")));

        assertThrows(CompletionException.class, () -> new RevokeDeviceUseCase(
                repository, authenticationService, mqttDynamicSecurityService)
                .execute(SERIAL_NUMBER, OWNER_ID, "USER"));

        assertEquals(ClaimStatus.CLAIMED, device.getClaimStatus());
        verify(authenticationService).revokeCertificate("certificate-serial");
        verify(repository, never()).save(any());
    }

    private Device claimedDevice() {
        return Device.restore(
                SERIAL_NUMBER, OWNER_ID,
                new Certificate("pem", "certificate-serial", Instant.now().plusSeconds(3600), Instant.now()),
                Instant.now(), ClaimStatus.CLAIMED, ConnectionStatus.ONLINE, null, "",
                Map.of(MetricsType.TEMPERATURE, 0.0), "", "redeem-hash", Instant.now());
    }
}
