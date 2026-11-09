package com.server.sensor_log.application.integration.support;

import com.server.sensor_log.domain.model.device.RandomGenerator;

final class TestIdentityGenerator implements RandomGenerator {

    @Override
    public String generateSerialNumber() {
        return "123456789012";
    }

    @Override
    public String generateBootstrapToken() {
        return "bootstrap-123";
    }

    @Override
    public String generateRedeemCode() {
        return "redeem-1234";
    }

    @Override
    public String generateUserId() {
        return "user-123";
    }
}
