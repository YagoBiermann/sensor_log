package com.server.sensor_log.domain.services;

public interface TokenGenerator {
    String generate();
    String toHash(String token);
}