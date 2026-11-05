package com.server.sensor_log.application.usecases;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.server.sensor_log.application.exceptions.DeviceNotFoundException;
import com.server.sensor_log.application.exceptions.InvalidDeviceStatusException;
import com.server.sensor_log.application.ports.Payload;
import com.server.sensor_log.application.ports.Topic;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.MetricsType;
import com.server.sensor_log.infra.repository.DeviceRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReceiveMetricsDataUseCase {
    private final DeviceRepository deviceRepository;

    public void execute(Topic topic, Payload payload) {
        if (topic == null || payload == null) {
            throw new IllegalArgumentException("Topic and payload must be provided");
        }
        if (!"data".equals(topic.getActionType())) {
            throw new IllegalArgumentException("Only data topics can update metrics");
        }
        if (!topic.getDeviceSerialNumber().equals(payload.getSerialNumber())) {
            throw new IllegalArgumentException("Topic and payload serial numbers do not match");
        }
        if (payload.getMetrics() == null || payload.getMetrics().isEmpty()) {
            throw new IllegalArgumentException("Payload must contain metrics");
        }

        Device device = deviceRepository.findById(topic.getDeviceSerialNumber())
                .orElseThrow(() -> new DeviceNotFoundException("Device not found"));

        if (device.isUnclaimed()) {
            throw new InvalidDeviceStatusException("Unable to publish metrics for unclaimed device {0}", device.getSerialNumber());
        }
        if (device.isRevoked()) {
            throw new InvalidDeviceStatusException("Unable to publish metrics for revoked device {0}", device.getSerialNumber());
        }
        List<MetricsType> payloadMetrics;
        payloadMetrics = new ArrayList<>(payload.getMetrics().keySet());
        if (!device.containsMetrics(payloadMetrics)) {
            throw new IllegalArgumentException("Payload contains metrics not configured for the device");
        }
        device.updateMetrics(payload.getMetrics());
        deviceRepository.save(device);
    }
}
