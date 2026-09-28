package com.server.sensor_log.domain.model.device;

import java.time.Instant;
import java.util.Map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class Device {

    private final String serialNumber;
    private final Instant createdAt;

    private String ownerId;
    private Instant claimedAt;

    private ClaimStatus claimStatus;
    private ConnectionStatus connectionStatus;

    private Location location;
    private Map<Metrics, Double> metrics;

    private final String bootstrapToken;
    private final String redeemToken;

    private Device(
            String serialNumber,
            Location location,
            Map<Metrics, Double> metrics,
            String bootstrapToken,
            String redeemToken,
            Instant createdAt) {
        this.serialNumber = serialNumber;
        this.location = location;
        this.metrics = Map.copyOf(metrics);
        this.bootstrapToken = bootstrapToken;
        this.redeemToken = redeemToken;
        this.createdAt = createdAt;

        this.claimStatus = ClaimStatus.UNCLAIMED;
        this.connectionStatus = ConnectionStatus.OFFLINE;
    }

    public static Device create(
            String serialNumber,
            Location location,
            Map<Metrics, Double> metrics,
            String redeemToken,
            String bootstrapToken) {
        return new Device(
                serialNumber,
                location,
                metrics,
                redeemToken,
                bootstrapToken,
                Instant.now());
    }

    public static Device restore(
            String serialNumber,
            Instant createdAt,
            String ownerId,
            Instant claimedAt,
            ClaimStatus claimStatus,
            ConnectionStatus connectionStatus,
            Location location,
            String bootstrapToken,
            String redeemToken,
            Map<Metrics, Double> metrics) {
        Device device = new Device(
                serialNumber,
                location,
                metrics,
                bootstrapToken,
                redeemToken,
                createdAt);

        device.ownerId = ownerId;
        device.claimedAt = claimedAt;
        device.claimStatus = claimStatus;
        device.connectionStatus = connectionStatus;

        return device;
    }
}