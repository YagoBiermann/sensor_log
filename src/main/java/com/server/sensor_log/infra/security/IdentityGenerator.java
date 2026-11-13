package com.server.sensor_log.infra.security;

import java.security.SecureRandom;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.server.sensor_log.domain.model.device.RandomGenerator;

@Component
public class IdentityGenerator implements RandomGenerator {
    private final SecureRandom RANDOM = new SecureRandom();
    private static final String TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    public String generateSerialNumber() {
        Integer length = 12;
        StringBuilder serial = new StringBuilder(length);

        serial.append(RANDOM.nextInt(1, 10));
        for (int i = 1; i < length; i++) {
            serial.append(RANDOM.nextInt(10));
        }

        return serial.toString();
    }

    @Override
    public String generateUserId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String generateBootstrapToken() {
        return generateToken(8);
    }

    @Override
    public String generateRedeemCode() {
        return generateToken(4);
    }

    private String generateToken(int length) {
        StringBuilder token = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            token.append(TOKEN_CHARS.charAt(RANDOM.nextInt(TOKEN_CHARS.length())));
        }
        return token.toString();
    }
}
