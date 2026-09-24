package com.server.sensor_log.domain.model.device;

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
    private DeviceType type = DeviceType.GENERIC;
    private final String bootstrap_token;

    public Device(String serialNumber, Location location, TokenGenerator tokenGenerator) {
        this.serialNumber = serialNumber;
        this.location = location;
        this.bootstrap_token = tokenGenerator.generate();
    }
}