package com.server.sensor_log.domain.services;

public interface TokenGenerator {
    String generate(int length);
    String toHash(String token);
}