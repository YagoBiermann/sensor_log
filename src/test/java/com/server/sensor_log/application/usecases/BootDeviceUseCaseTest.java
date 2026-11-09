package com.server.sensor_log.application.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.server.sensor_log.application.controllers.device.dto.FirstBootRequest;
import com.server.sensor_log.application.exceptions.DeviceNotFoundException;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.ClaimStatus;
import com.server.sensor_log.domain.model.device.ConnectionStatus;
import com.server.sensor_log.domain.model.device.MetricsType;
import com.server.sensor_log.domain.model.device.RandomGenerator;
import com.server.sensor_log.infra.repository.DeviceRepository;

@ExtendWith(MockitoExtension.class)
class BootDeviceUseCaseTest {
    @Mock DeviceRepository repository;
    @Mock RandomGenerator generator;
    @Mock PasswordEncoder encoder;

    @Test
    void bootsAndReturnsPlainRedeemCode() {
        Device device = Device.create("123456789012", "bootstrap-hash", "", null,
                List.of(MetricsType.TEMPERATURE));
        when(repository.findById("123456789012")).thenReturn(Optional.of(device));
        when(encoder.matches("bootstrap", "bootstrap-hash")).thenReturn(true);
        when(generator.generateRedeemCode()).thenReturn("redeem");
        when(encoder.encode("redeem")).thenReturn("redeem-hash");

        var response = new BootDeviceUseCase(repository, generator, encoder)
                .execute(new FirstBootRequest("123456789012", "bootstrap"));

        assertEquals("redeem", response.redeemCode());
        assertEquals("123456789012", device.getSerialNumber());
        assertEquals(ClaimStatus.UNCLAIMED, device.getClaimStatus());
        assertEquals(ConnectionStatus.ONLINE, device.getConnectionStatus());
        assertEquals("", device.getBootstrapToken());
        assertEquals("redeem-hash", device.getRedeemCode());
        verify(encoder).matches("bootstrap", "bootstrap-hash");
        verify(generator).generateRedeemCode();
        verify(encoder).encode("redeem");
        verify(repository).save(device);
    }

    @Test
    void rejectsMissingDevice() {
        when(repository.findById("123456789012")).thenReturn(Optional.empty());
        assertThrows(DeviceNotFoundException.class, () -> new BootDeviceUseCase(repository, generator, encoder)
                .execute(new FirstBootRequest("123456789012", "bootstrap")));
        verifyNoInteractions(encoder, generator);
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsInvalidBootstrapToken() {
        Device device = Device.create("123456789012", "bootstrap-hash", "", null,
                List.of(MetricsType.TEMPERATURE));
        when(repository.findById("123456789012")).thenReturn(Optional.of(device));
        when(encoder.matches("wrong", "bootstrap-hash")).thenReturn(false);

        assertThrows(SecurityException.class, () -> new BootDeviceUseCase(repository, generator, encoder)
                .execute(new FirstBootRequest("123456789012", "wrong")));

        assertEquals(ConnectionStatus.OFFLINE, device.getConnectionStatus());
        assertEquals("bootstrap-hash", device.getBootstrapToken());
        assertEquals("", device.getRedeemCode());
        verify(encoder).matches("wrong", "bootstrap-hash");
        verifyNoInteractions(generator);
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsDeviceThatWasAlreadyBooted() {
        Device device = Device.create("123456789012", "bootstrap-hash", "", null,
                List.of(MetricsType.TEMPERATURE));
        when(repository.findById("123456789012")).thenReturn(Optional.of(device));
        when(encoder.matches("bootstrap", "bootstrap-hash")).thenReturn(true);
        when(generator.generateRedeemCode()).thenReturn("redeem");
        when(encoder.encode("redeem")).thenReturn("redeem-hash");

        new BootDeviceUseCase(repository, generator, encoder)
                .execute(new FirstBootRequest("123456789012", "bootstrap"));

        when(encoder.matches("bootstrap", "")).thenReturn(true);

        assertThrows(SecurityException.class, () -> new BootDeviceUseCase(repository, generator, encoder)
                .execute(new FirstBootRequest("123456789012", "bootstrap")));

        verify(generator, times(1)).generateRedeemCode();
        verify(repository, times(1)).save(device);
    }

    @Test
    void rejectsDeviceWithUnexpectedSerialNumber() {
        Device device = Device.create("999999999999", "bootstrap-hash", "", null,
                List.of(MetricsType.TEMPERATURE));
        when(repository.findById("123456789012")).thenReturn(Optional.of(device));

        assertThrows(SecurityException.class, () -> new BootDeviceUseCase(repository, generator, encoder)
                .execute(new FirstBootRequest("123456789012", "bootstrap")));

        verifyNoInteractions(encoder, generator);
        verify(repository, never()).save(any());
    }
}
