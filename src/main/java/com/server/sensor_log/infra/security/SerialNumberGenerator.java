package com.server.sensor_log.infra.security;

import java.security.SecureRandom;

import com.server.sensor_log.domain.model.device.RandomGenerator;

public class SerialNumberGenerator implements RandomGenerator {

    private final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generate(int length) {
        StringBuilder serial = new StringBuilder(length);

        serial.append(RANDOM.nextInt(1, 10));
        for (int i = 1; i < length; i++) {
            serial.append(RANDOM.nextInt(10));
        }

        return serial.toString();
    }
}