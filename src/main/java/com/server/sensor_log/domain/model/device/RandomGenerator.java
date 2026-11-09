package com.server.sensor_log.domain.model.device;

public interface RandomGenerator {
    String generateRedeemCode();
    String generateBootstrapToken();
    String generateSerialNumber();
    String generateUserId();
}