package com.server.sensor_log.application.services;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {
    private static final String SECRET = "a-secret-key-that-is-at-least-32-bytes-long";

    @Test
    void generatesAndExtractsClaims() {
        JwtService service = new JwtService(SECRET);

        String token = service.generateToken("user-1", "USER");

        assertEquals("user-1", service.extractUserId(token));
        assertEquals("USER", service.extractRole(token));
    }

    @Test
    void rejectsTamperedToken() {
        JwtService service = new JwtService(SECRET);
        String token = service.generateToken("user-1", "USER");

        assertThrows(RuntimeException.class, () -> service.extractUserId(token + "x"));
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        JwtService service = new JwtService(SECRET);
        String token = Jwts.builder()
                .subject("user-1")
                .claim("role", "USER")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor("another-secret-key-that-is-at-least-32-bytes".getBytes()))
                .compact();

        assertThrows(RuntimeException.class, () -> service.extractUserId(token));
    }

    @Test
    void rejectsShortSecret() {
        assertThrows(RuntimeException.class, () -> new JwtService("short"));
    }
}
