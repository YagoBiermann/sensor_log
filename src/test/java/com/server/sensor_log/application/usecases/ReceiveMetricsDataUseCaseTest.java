package com.server.sensor_log.application.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.server.sensor_log.application.exceptions.DeviceNotFoundException;
import com.server.sensor_log.application.exceptions.InvalidDeviceStatusException;
import com.server.sensor_log.application.ports.Payload;
import com.server.sensor_log.application.ports.Topic;
import com.server.sensor_log.domain.model.device.ClaimStatus;
import com.server.sensor_log.domain.model.device.ConnectionStatus;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.MetricsType;
import com.server.sensor_log.infra.repository.DeviceRepository;

@ExtendWith(MockitoExtension.class)
class ReceiveMetricsDataUseCaseTest {
    @Mock DeviceRepository repository;

    @Test
    void updatesConfiguredMetrics() {
        Device device = claimedDevice(MetricsType.TEMPERATURE);
        when(repository.findById("123456789012")).thenReturn(Optional.of(device));

        new ReceiveMetricsDataUseCase(repository).execute(
                new Topic("123456789012", "data"),
                new Payload("123456789012", Map.of(MetricsType.TEMPERATURE, 22.5)));

        assertEquals(22.5, device.getMetrics().get(MetricsType.TEMPERATURE));
        verify(repository).save(device);
    }

    @Test
    void updatesMultipleConfiguredMetricsAndPreservesExistingValues() {
        Device device = claimedDevice(
                MetricsType.TEMPERATURE, MetricsType.HUMIDITY, MetricsType.PRESSURE);
        device.updateMetrics(Map.of(
                MetricsType.TEMPERATURE, 18.0,
                MetricsType.HUMIDITY, 40.0,
                MetricsType.PRESSURE, 1013.0));
        when(repository.findById("123456789012")).thenReturn(Optional.of(device));

        new ReceiveMetricsDataUseCase(repository).execute(
                new Topic("123456789012", "data"),
                new Payload("123456789012", Map.of(
                        MetricsType.TEMPERATURE, 22.5,
                        MetricsType.PRESSURE, 1008.2)));

        assertEquals(22.5, device.getMetrics().get(MetricsType.TEMPERATURE));
        assertEquals(40.0, device.getMetrics().get(MetricsType.HUMIDITY));
        assertEquals(1008.2, device.getMetrics().get(MetricsType.PRESSURE));
        verify(repository).save(device);
    }

    @Test
    void rejectsCommandTopicAndMismatchedSerial() {
        Payload payload = new Payload("123456789012", Map.of(MetricsType.TEMPERATURE, 22.5));
        var useCase = new ReceiveMetricsDataUseCase(repository);

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(new Topic("123456789012", "command"), payload));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(new Topic("999999999999", "data"), payload));
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsMetricsNotConfiguredForDevice() {
        Device device = claimedDevice(MetricsType.TEMPERATURE);
        when(repository.findById("123456789012")).thenReturn(Optional.of(device));

        assertThrows(IllegalArgumentException.class, () -> new ReceiveMetricsDataUseCase(repository).execute(
                new Topic("123456789012", "data"),
                new Payload("123456789012", Map.of(MetricsType.HUMIDITY, 50.0))));
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsMetricsForUnclaimedDevice() {
        Device device = Device.create("123456789012", "", "", null, List.of(MetricsType.TEMPERATURE));
        when(repository.findById("123456789012")).thenReturn(Optional.of(device));

        assertThrows(InvalidDeviceStatusException.class, () -> new ReceiveMetricsDataUseCase(repository).execute(
                new Topic("123456789012", "data"),
                new Payload("123456789012", Map.of(MetricsType.TEMPERATURE, 22.5))));
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsUnknownDevice() {
        when(repository.findById("123456789012")).thenReturn(Optional.empty());

        assertThrows(DeviceNotFoundException.class, () -> new ReceiveMetricsDataUseCase(repository).execute(
                new Topic("123456789012", "data"),
                new Payload("123456789012", Map.of(MetricsType.TEMPERATURE, 22.5))));

        verify(repository).findById("123456789012");
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsEmptyPayloadWithoutLookingUpDevice() {
        var useCase = new ReceiveMetricsDataUseCase(repository);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(
                new Topic("123456789012", "data"),
                new Payload("123456789012", Map.of())));

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsNullTopicOrPayload() {
        var useCase = new ReceiveMetricsDataUseCase(repository);
        Payload payload = new Payload("123456789012", Map.of(MetricsType.TEMPERATURE, 22.5));

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null, payload));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(
                new Topic("123456789012", "data"), null));

        verifyNoInteractions(repository);
    }

    private Device claimedDevice(MetricsType... metrics) {
        Map<MetricsType, Double> metricValues = new EnumMap<>(MetricsType.class);
        for (MetricsType metric : metrics) {
            metricValues.put(metric, 0.0);
        }
        Instant now = Instant.now();
        return Device.restore(
                "123456789012",
                "owner",
                null,
                now,
                ClaimStatus.CLAIMED,
                ConnectionStatus.ONLINE,
                null,
                "",
                metricValues,
                "",
                "",
                now);
    }
}
