package com.server.sensor_log.domain.model.device;

import java.util.Set;

import com.server.sensor_log.domain.services.TokenGenerator;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class Device {
    private String serialNumber;
    private ClaimStatus claimStatus = ClaimStatus.UNCLAIMED;
    private ConnectionStatus connectionStatus = ConnectionStatus.OFFLINE;
    private Location location;
    private final String bootstrap_token;
    private final String redeem_token;
    private Set<MetricType> metrics;

    public Device(String serialNumber, Location location, Set<MetricType> metrics, TokenGenerator tokenGenerator) {
        this.serialNumber = serialNumber;
        this.location = location;
        this.metrics = metrics;
        this.bootstrap_token = tokenGenerator.generate(32);
        this.redeem_token = tokenGenerator.generate(4);
    }
}