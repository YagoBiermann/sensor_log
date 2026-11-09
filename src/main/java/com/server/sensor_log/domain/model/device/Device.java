package com.server.sensor_log.domain.model.device;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.Getter;

@Getter
@Document(collection = "devices")
public class Device {
    @Id
    private final String serialNumber;
    private final Role role = Role.DEVICE;
    private final Instant createdAt;
    private final String firmwareVersion = "1.0.0";
    private String description;

    private String ownerId;
    private ClaimStatus claimStatus;
    private Instant claimedAt;
    private Certificate certificate;

    private ConnectionStatus connectionStatus;

    private Location location;
    private Map<MetricsType, Double> metrics;

    private String bootstrapToken;
    private String redeemCode;

    @PersistenceCreator
    private Device(
            String serialNumber,
            String ownerId,
            Certificate certificate,
            ClaimStatus claimStatus,
            Instant claimedAt,
            ConnectionStatus connectionStatus,
            Location location,
            String description,
            Map<MetricsType, Double> metrics,
            String bootstrapToken,
            String redeemCode,
            Instant createdAt) {
        this.serialNumber = serialNumber;
        this.ownerId = ownerId;
        this.certificate = certificate;
        this.claimStatus = claimStatus;
        this.claimedAt = claimedAt;
        this.connectionStatus = connectionStatus;
        this.location = location;
        this.description = description;
        this.bootstrapToken = bootstrapToken;
        this.redeemCode = redeemCode;
        this.createdAt = createdAt;
        this.metrics = new EnumMap<>(MetricsType.class);
        metrics.forEach((key, value) -> {
            this.metrics.put(key, value);
        });
    }

    private Device(
            String serialNumber,
            String bootstrapToken,
            String description,
            Location location,
            List<MetricsType> metrics) {
        this.serialNumber = serialNumber;
        this.location = location;
        this.description = description;
        this.bootstrapToken = bootstrapToken;
        this.certificate = null;
        this.redeemCode = "";
        this.createdAt = Instant.now();
        this.metrics = new EnumMap<>(MetricsType.class);
        metrics.forEach(mt -> {
            this.metrics.put(mt, 0.0);
        });
        this.claimStatus = ClaimStatus.UNCLAIMED;
        this.connectionStatus = ConnectionStatus.OFFLINE;
    }

    public static Device create(
            String serialNumber,
            String bootstrapToken,
            String description,
            Location location,
            List<MetricsType> metrics) {

        return new Device(
                serialNumber,
                bootstrapToken,
                description,
                location,
                metrics);
    }

    public static Device restore(
            String serialNumber,
            String ownerId,
            Certificate certificate,
            Instant claimedAt,
            ClaimStatus claimStatus,
            ConnectionStatus connectionStatus,
            Location location,
            String description,
            Map<MetricsType, Double> metrics,
            String bootstrapToken,
            String redeemCode,
            Instant createdAt) {
        Device device = new Device(
                serialNumber,
                ownerId,
                certificate,
                claimStatus,
                claimedAt,
                connectionStatus,
                location,
                description,
                metrics,
                bootstrapToken,
                redeemCode,
                createdAt);

        return device;
    }

    public String boot(String serialNumber, String bootstrapToken, RandomGenerator identityGenerator,
            PasswordEncoder passwordEncoder)
            throws SecurityException {
        if (!this.serialNumber.equals(serialNumber)) {
            throw new SecurityException("Invalid serial number.");
        }
        if (!passwordEncoder.matches(bootstrapToken, this.bootstrapToken)) {
            throw new SecurityException("Invalid token.");
        }
        if (this.claimStatus != ClaimStatus.UNCLAIMED || this.bootstrapToken.isEmpty()) {
            throw new SecurityException("Device was already booted.");
        }

        this.connectionStatus = ConnectionStatus.ONLINE;
        this.bootstrapToken = "";
        String plainRedeemCode = identityGenerator.generateRedeemCode();
        this.redeemCode = passwordEncoder.encode(plainRedeemCode);
        return plainRedeemCode;
    }

    public void claim(String ownerId, String redeemCode, Certificate certificate, PasswordEncoder passwordEncoder)
            throws SecurityException {
        if (this.connectionStatus != ConnectionStatus.ONLINE || !this.bootstrapToken.isEmpty()) {
            throw new SecurityException("Please, boot the device first.");
        }
        if (this.claimStatus != ClaimStatus.UNCLAIMED) {
            throw new SecurityException("Device property already confirmed.");
        }
        if (!passwordEncoder.matches(redeemCode, this.redeemCode)) {
            throw new SecurityException("Invalid redeem token.");
        }
        this.ownerId = ownerId;
        this.redeemCode = passwordEncoder.encode(redeemCode);
        this.claimStatus = ClaimStatus.CLAIMED;
        this.connectionStatus = ConnectionStatus.ONLINE;
        this.certificate = certificate;
        this.claimedAt = Instant.now();
    }

    public boolean isClaimed() {
        return this.claimStatus == ClaimStatus.CLAIMED;
    }
    public boolean isRevoked() {
        return this.claimStatus == ClaimStatus.REVOKED;
    }
    public boolean isUnclaimed() {
        return this.claimStatus == ClaimStatus.UNCLAIMED;
    }
    public void revoke() {
        if (!isClaimed()) {
            throw new SecurityException("Device is not claimed");
        }
        this.claimStatus = ClaimStatus.REVOKED;
        this.connectionStatus = ConnectionStatus.OFFLINE;
    }

    public boolean containsMetrics(List<MetricsType> metrics) {
        return this.metrics.keySet().containsAll(metrics);
    }

    public void setLocation(Location newLocation) {
        this.location = newLocation;
    }

    public void changeDescription(String newDescription) {
        this.description = newDescription;
    }

    public void setRedeemCode(String redeemCode) {
        if (this.claimStatus != ClaimStatus.UNCLAIMED || this.redeemCode.isEmpty()) {
            throw new SecurityException("Device was already claimed.");
        }
        this.redeemCode = redeemCode;
    }

    public void updateMetrics(Map<MetricsType, Double> newMetrics) {
        newMetrics.forEach((key, value) -> {
            this.metrics.put(key, value);
        });
    }

    public Map<MetricsType, Double> getMetrics() {
        return new EnumMap<>(this.metrics);
    }
}