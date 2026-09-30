package com.server.sensor_log.domain.model.device;

import java.time.Instant;
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
    private Metrics metrics;

    private String bootstrapToken;
    private String redeemToken;

    private Device(
            String serialNumber,
            Location location,
            String description,
            Metrics metrics,
            String bootstrapToken,
            String redeemToken,
            Instant createdAt) {
        this.serialNumber = serialNumber;
        this.location = location;
        this.description = description;
        this.metrics = metrics;
        this.bootstrapToken = bootstrapToken;
        this.redeemToken = redeemToken;
        this.createdAt = createdAt;

        this.claimStatus = ClaimStatus.UNCLAIMED;
        this.connectionStatus = ConnectionStatus.OFFLINE;
    }

    public static Device create(
            RandomGenerator randomGenerator,
            Location location,
            String description,
            Metrics metrics) {

        String serialNumber = randomGenerator.generate(8);
        String redeemToken = randomGenerator.generate(6);
        String bootstrapToken = randomGenerator.generate(12);

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
            Instant createdAt,
            String ownerId,
            Instant claimedAt,
            ClaimStatus claimStatus,
            ConnectionStatus connectionStatus,
            Location location,
            String description,
            String bootstrapToken,
            String redeemToken,
            Metrics metrics) {
        Device device = new Device(
                serialNumber,
                location,
                description,
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

    public void claim(String ownerId, String bootstrapToken, String redeemToken) {
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