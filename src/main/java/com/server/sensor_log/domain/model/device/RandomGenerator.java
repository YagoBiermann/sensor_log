package com.server.sensor_log.domain.model.device;

public interface RandomGenerator {
    String generateRedeemToken();
    String generateBootstrapToken();
    String generateSerialNumber();
    String generateUserId();
    String enconde(String token);
}