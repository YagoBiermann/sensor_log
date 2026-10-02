package com.server.sensor_log.domain.model.device;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@Document(collection = "devices")
public class Device {
    @Id
    private final String serialNumber;
    private final Instant createdAt;
    private String description;

    private String ownerId;
    private Instant claimedAt;

    private ClaimStatus claimStatus;
    private ConnectionStatus connectionStatus;

    private Location location;
    private Map<MetricsType, Double> metrics;

    private String bootstrapToken;
    private String redeemToken;

    private Device(
            String serialNumber,
            String ownerId,
            ClaimStatus claimStatus,
            Instant claimedAt,
            ConnectionStatus connectionStatus,
            Location location,
            String description,
            Map<MetricsType, Double> metrics,
            String bootstrapToken,
            String redeemToken,
            Instant createdAt) {
        this.serialNumber = serialNumber;
        this.ownerId = ownerId;
        this.claimStatus = claimStatus;
        this.claimedAt = claimedAt;
        this.connectionStatus = connectionStatus;
        this.location = location;
        this.description = description;
        this.bootstrapToken = bootstrapToken;
        this.redeemToken = redeemToken;
        this.createdAt = createdAt;
        this.metrics = new EnumMap<>(MetricsType.class);
        metrics.forEach((key, value) -> {
            this.metrics.put(key, value);
        });
    }

    private Device(
            String serialNumber,
            Location location,
            String description,
            List<MetricsType> metrics,
            String bootstrapToken,
            String redeemToken,
            Instant createdAt) {
        this.serialNumber = serialNumber;
        this.location = location;
        this.description = description;
        this.bootstrapToken = bootstrapToken;
        this.redeemToken = redeemToken;
        this.createdAt = createdAt;
        this.metrics = new EnumMap<>(MetricsType.class);
        metrics.forEach(mt -> {
            this.metrics.put(mt, 0.0);
        });
        this.claimStatus = ClaimStatus.UNCLAIMED;
        this.connectionStatus = ConnectionStatus.OFFLINE;
    }

    public static Device create(
            RandomGenerator randomGenerator,
            Location location,
            String description,
            List<MetricsType> metrics) {

        String serialNumber = randomGenerator.generateSerialNumber();
        String redeemToken = randomGenerator.generateRedeemToken();
        String bootstrapToken = randomGenerator.generateBootstrapToken();

        return new Device(
                serialNumber,
                location,
                description,
                metrics,
                bootstrapToken,
                redeemToken,
                Instant.now());
    }

    public static Device restore(
            String serialNumber,
            String ownerId,
            Instant claimedAt,
            ClaimStatus claimStatus,
            ConnectionStatus connectionStatus,
            Location location,
            String description,
            Map<MetricsType, Double> metrics,
            String bootstrapToken,
            String redeemToken,
            Instant createdAt) {
        Device device = new Device(
                serialNumber,
                ownerId,
                claimStatus,
                claimedAt,
                connectionStatus,
                location,
                description,
                metrics,
                bootstrapToken,
                redeemToken,
                createdAt);

        return device;
    }

    public void claim(String ownerId, String bootstrapToken, String redeemToken) throws SecurityException {
        if (this.claimStatus != ClaimStatus.UNCLAIMED) {
            throw new SecurityException("Device was already claimed.");
        }

        this.ownerId = ownerId;
        this.bootstrapToken = null;
        this.redeemToken = null;

        this.claimedAt = Instant.now();
    }

    public void setLocation(Location newLocation) {
        this.location = newLocation;
    }

    public void changeDescription(String newDescription) {
        this.description = newDescription;
    }
}