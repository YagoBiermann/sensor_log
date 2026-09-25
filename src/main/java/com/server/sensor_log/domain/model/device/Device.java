package com.server.sensor_log.domain.model.device;

import java.time.Instant;
import java.util.Set;

import com.server.sensor_log.domain.services.TokenGenerator;

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
    private Set<MetricType> metrics;

    private final String bootstrapToken;
    private final String redeemToken;

    private Device(
            String serialNumber,
            Location location,
            Set<MetricType> metrics,
            String bootstrapToken,
            String redeemToken,
            Instant createdAt) {
        this.serialNumber = serialNumber;
        this.location = location;
        this.metrics = Set.copyOf(metrics);
        this.bootstrapToken = bootstrapToken;
        this.redeemToken = redeemToken;
        this.createdAt = createdAt;

        this.claimStatus = ClaimStatus.UNCLAIMED;
        this.connectionStatus = ConnectionStatus.OFFLINE;
    }

    public static Device create(
            String serialNumber,
            Location location,
            Set<MetricType> metrics,
            TokenGenerator tokenGenerator) {
        return new Device(
                serialNumber,
                location,
                metrics,
                tokenGenerator.generate(32),
                tokenGenerator.generate(4),
                Instant.now());
    }
}