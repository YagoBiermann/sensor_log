package com.server.sensor_log.infra.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import com.server.sensor_log.domain.model.device.RandomGenerator;

public class IdentityGenerator implements RandomGenerator {
    private final SecureRandom RANDOM = new SecureRandom();

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
    public String generateRedeemToken() {
        return generateToken(4);
    }

    private String generateToken(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    @Override
    public String encode(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
