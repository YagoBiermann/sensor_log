package com.server.sensor_log.application.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.server.sensor_log.application.controllers.device.dto.NewDevice;
import com.server.sensor_log.domain.model.device.ClaimStatus;
import com.server.sensor_log.domain.model.device.ConnectionStatus;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.MetricsType;
import com.server.sensor_log.domain.model.device.RandomGenerator;
import com.server.sensor_log.infra.repository.DeviceRepository;

@ExtendWith(MockitoExtension.class)
class CreateDeviceUseCaseTest {
    @Mock DeviceRepository repository;
    @Mock RandomGenerator generator;
    @Mock PasswordEncoder encoder;

    @Test
    void createsDeviceWithPlainSerialAndHashedBootstrapToken() {
        when(generator.generateSerialNumber()).thenReturn("123456789012");
        when(generator.generateBootstrapToken()).thenReturn("bootstrap");
        when(repository.existsById("123456789012")).thenReturn(false);
        when(encoder.encode("bootstrap")).thenReturn("hashed-bootstrap");

        var response = new CreateDeviceUseCase(repository, generator, encoder)
                .execute(new NewDevice(List.of(MetricsType.TEMPERATURE)));

        assertEquals("123456789012", response.serialNumber());
        assertEquals("bootstrap", response.bootstrapToken());
        ArgumentCaptor<Device> captor = ArgumentCaptor.forClass(Device.class);
        verify(repository).save(captor.capture());
        assertEquals("123456789012", captor.getValue().getSerialNumber());
        assertEquals(List.of(MetricsType.TEMPERATURE), List.copyOf(captor.getValue().getMetrics().keySet()));
        assertEquals("hashed-bootstrap", captor.getValue().getBootstrapToken());
        assertEquals("", captor.getValue().getDescription());
        assertEquals("", captor.getValue().getRedeemToken());
        assertEquals(ClaimStatus.UNCLAIMED, captor.getValue().getClaimStatus());
        assertEquals(ConnectionStatus.OFFLINE, captor.getValue().getConnectionStatus());
        assertEquals(0.0, captor.getValue().getLocation().latitude());
        assertEquals(0.0, captor.getValue().getLocation().longitude());
        assertEquals(0.0, captor.getValue().getMetrics().get(MetricsType.TEMPERATURE));
        verify(generator).generateSerialNumber();
        verify(generator).generateBootstrapToken();
        verify(encoder).encode("bootstrap");
    }

    @Test
    void retriesCollidingSerial() {
        when(generator.generateSerialNumber()).thenReturn("123456789012", "123456789013");
        when(generator.generateBootstrapToken()).thenReturn("bootstrap");
        when(repository.existsById("123456789012")).thenReturn(true);
        when(repository.existsById("123456789013")).thenReturn(false);
        when(encoder.encode("bootstrap")).thenReturn("hash");

        var response = new CreateDeviceUseCase(repository, generator, encoder)
                .execute(new NewDevice(List.of(MetricsType.HUMIDITY)));

        assertEquals("123456789013", response.serialNumber());
        verify(repository).save(any(Device.class));
        verify(generator, times(2)).generateSerialNumber();
        verify(generator).generateBootstrapToken();
        verify(encoder).encode("bootstrap");
    }

    @Test
    void retriesUntilFindingAnAvailableSerial() {
        when(generator.generateSerialNumber()).thenReturn(
                "123456789012", "123456789013", "123456789014");
        when(generator.generateBootstrapToken()).thenReturn("bootstrap");
        when(repository.existsById("123456789012")).thenReturn(true);
        when(repository.existsById("123456789013")).thenReturn(true);
        when(repository.existsById("123456789014")).thenReturn(false);
        when(encoder.encode("bootstrap")).thenReturn("hash");

        var response = new CreateDeviceUseCase(repository, generator, encoder)
                .execute(new NewDevice(List.of(MetricsType.POWER)));

        assertEquals("123456789014", response.serialNumber());
        verify(repository, times(3)).existsById(anyString());
        verify(repository).save(any(Device.class));
    }

    @Test
    void createsDeviceWithAllConfiguredMetricsInitializedToZero() {
        List<MetricsType> metrics = List.of(MetricsType.TEMPERATURE, MetricsType.HUMIDITY,
                MetricsType.CO2, MetricsType.VOLTAGE);
        when(generator.generateSerialNumber()).thenReturn("123456789012");
        when(generator.generateBootstrapToken()).thenReturn("bootstrap");
        when(repository.existsById("123456789012")).thenReturn(false);
        when(encoder.encode("bootstrap")).thenReturn("hash");

        new CreateDeviceUseCase(repository, generator, encoder).execute(new NewDevice(metrics));

        ArgumentCaptor<Device> captor = ArgumentCaptor.forClass(Device.class);
        verify(repository).save(captor.capture());
        Device device = captor.getValue();
        assertEquals(metrics.size(), device.getMetrics().size());
        metrics.forEach(metric -> assertEquals(0.0, device.getMetrics().get(metric)));
        assertEquals(metrics, List.copyOf(device.getMetrics().keySet()));
    }

    @Test
    void rejectsNullDeviceDataBeforeGeneratingCredentials() {
        var useCase = new CreateDeviceUseCase(repository, generator, encoder);

        assertThrows(NullPointerException.class, () -> useCase.execute(null));

        verifyNoInteractions(repository, generator, encoder);
    }

    @Test
    void rejectsMissingMetrics() {
        var useCase = new CreateDeviceUseCase(repository, generator, encoder);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(new NewDevice(List.of())));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(new NewDevice(null)));
        verifyNoInteractions(repository, generator, encoder);
    }
}
