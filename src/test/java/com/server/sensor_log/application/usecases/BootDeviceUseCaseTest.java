package com.server.sensor_log.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.server.sensor_log.application.controllers.device.dto.FirstBootRequest;
import com.server.sensor_log.application.exceptions.DeviceNotFoundException;
import com.server.sensor_log.domain.model.device.ClaimStatus;
import com.server.sensor_log.domain.model.device.ConnectionStatus;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.MetricsType;
import com.server.sensor_log.domain.model.device.RandomGenerator;
import com.server.sensor_log.domain.model.device.RedeemCode;
import com.server.sensor_log.infra.repository.DeviceRepository;

@ExtendWith(MockitoExtension.class)
class BootDeviceUseCaseTest {
    private static final String SERIAL = "123456789012";
    private static final String BOOTSTRAP_TOKEN = "bootstrap";

    @Mock DeviceRepository repository;
    @Mock RandomGenerator generator;
    @Mock PasswordEncoder encoder;

    @Test
    void bootsAnUnclaimedDeviceAndReturnsRedeemCodeAndState() {
        Device device = unclaimedDevice();
        when(repository.findById(SERIAL)).thenReturn(Optional.of(device));
        when(encoder.matches(BOOTSTRAP_TOKEN, "bootstrap-hash")).thenReturn(true);
        when(generator.generateRedeemCode()).thenReturn("redeem");

        var response = useCase().execute(new FirstBootRequest(SERIAL, BOOTSTRAP_TOKEN));

        assertEquals("redeem", response.redeemCode());
        assertEquals(ClaimStatus.PENDING.toString(), response.claimStatus());
        assertEquals(ConnectionStatus.ONLINE.toString(), response.connectionStatus());
        assertFalse(response.inCooldown());
        assertFalse(response.hasExpired());
        assertEquals(ClaimStatus.PENDING, device.getClaimStatus());
        assertEquals(ConnectionStatus.ONLINE, device.getConnectionStatus());
        assertEquals("bootstrap-hash", device.getBootstrapToken());
        assertEquals("redeem", device.getRedeemCode().toString());
        verify(generator).generateRedeemCode();
        verify(repository).save(device);
    }

    @Test
    void rejectsMissingDeviceWithoutGeneratingOrSaving() {
        when(repository.findById(SERIAL)).thenReturn(Optional.empty());

        assertThrows(DeviceNotFoundException.class,
                () -> useCase().execute(new FirstBootRequest(SERIAL, BOOTSTRAP_TOKEN)));

        verifyNoInteractions(encoder, generator);
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsMissingOrInvalidBootstrapToken() {
        Device device = unclaimedDevice();
        when(repository.findById(SERIAL)).thenReturn(Optional.of(device));
        when(encoder.matches("wrong", "bootstrap-hash")).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> useCase().execute(new FirstBootRequest(SERIAL, "")));
        assertThrows(SecurityException.class,
                () -> useCase().execute(new FirstBootRequest(SERIAL, "wrong")));

        assertEquals(ClaimStatus.UNCLAIMED, device.getClaimStatus());
        assertEquals(ConnectionStatus.OFFLINE, device.getConnectionStatus());
        verifyNoInteractions(generator);
        verify(repository, times(2)).save(device);
    }

    @Test
    void allowsRebootAfterThePendingRedeemCodeExpires() {
        RedeemCode expiredCode = RedeemCode.restore(
                "old-code", Instant.now().minusSeconds(1), 0, Instant.EPOCH, generator);
        Device device = Device.restore(SERIAL, null, null, null, ClaimStatus.PENDING,
                ConnectionStatus.ONLINE, null, "", java.util.Map.of(MetricsType.TEMPERATURE, 0.0),
                "bootstrap-hash", expiredCode, Instant.now());
        when(repository.findById(SERIAL)).thenReturn(Optional.of(device));
        when(encoder.matches(BOOTSTRAP_TOKEN, "bootstrap-hash")).thenReturn(true);
        when(generator.generateRedeemCode()).thenReturn("new-code");

        var response = useCase().execute(new FirstBootRequest(SERIAL, BOOTSTRAP_TOKEN));

        assertEquals("new-code", response.redeemCode());
        assertEquals("new-code", device.getRedeemCode().toString());
        verify(generator).generateRedeemCode();
        verify(repository).save(device);
    }

    @Test
    void rejectsBootOfClaimedOrRevokedDevice() {
        Device claimed = Device.restore(SERIAL, "owner", null, Instant.now(), ClaimStatus.CLAIMED,
                ConnectionStatus.ONLINE, null, "", java.util.Map.of(MetricsType.TEMPERATURE, 0.0),
                "bootstrap-hash", null, Instant.now());
        Device revoked = Device.restore(SERIAL, "owner", null, Instant.now(), ClaimStatus.REVOKED,
                ConnectionStatus.OFFLINE, null, "", java.util.Map.of(MetricsType.TEMPERATURE, 0.0),
                "bootstrap-hash", null, Instant.now());
        when(repository.findById(SERIAL)).thenReturn(Optional.of(claimed), Optional.of(revoked));
        when(encoder.matches(BOOTSTRAP_TOKEN, "bootstrap-hash")).thenReturn(true);

        assertThrows(SecurityException.class,
                () -> useCase().execute(new FirstBootRequest(SERIAL, BOOTSTRAP_TOKEN)));
        assertThrows(SecurityException.class,
                () -> useCase().execute(new FirstBootRequest(SERIAL, BOOTSTRAP_TOKEN)));

        verifyNoInteractions(generator);
        verify(repository, times(2)).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsBootWhenRedeemCodeIsStillValid() {
        Device device = Device.restore(SERIAL, null, null, null, ClaimStatus.PENDING,
                ConnectionStatus.ONLINE, null, "", java.util.Map.of(MetricsType.TEMPERATURE, 0.0),
                "bootstrap-hash",
                RedeemCode.restore("redeem", Instant.now().plusSeconds(60), 0, Instant.EPOCH, generator),
                Instant.now());
        when(repository.findById(SERIAL)).thenReturn(Optional.of(device));
        when(encoder.matches(BOOTSTRAP_TOKEN, "bootstrap-hash")).thenReturn(true);

        assertThrows(SecurityException.class,
                () -> useCase().execute(new FirstBootRequest(SERIAL, BOOTSTRAP_TOKEN)));

        verifyNoInteractions(generator);
        verify(repository).save(device);
    }

    private Device unclaimedDevice() {
        return Device.create(SERIAL, "bootstrap-hash", "", null, List.of(MetricsType.TEMPERATURE));
    }

    private BootDeviceUseCase useCase() {
        return new BootDeviceUseCase(repository, generator, encoder);
    }
}
